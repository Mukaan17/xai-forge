package com.xaiforge.infrastructure.ml.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tribuo.MutableDataset;
import org.tribuo.Example;
import org.tribuo.classification.Label;
import org.tribuo.regression.Regressor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Utility for splitting datasets into training and testing sets
 * 
 * @since 1.0.0
 */
@Component
@Slf4j
public class DatasetSplitter {
    
    /**
     * Result of dataset splitting containing train and test datasets
     */
    public static class SplitResult {
        private final MutableDataset<?> train;
        private final MutableDataset<?> test;
        
        public SplitResult(MutableDataset<?> train, MutableDataset<?> test) {
            this.train = train;
            this.test = test;
        }
        
        public MutableDataset<?> getTrain() {
            return train;
        }
        
        public MutableDataset<?> getTest() {
            return test;
        }
    }
    
    /**
     * Split a dataset into training and testing sets
     * 
     * @param dataset The full dataset
     * @param trainTestSplit The percentage of data to use for training (50-90)
     * @return A SplitResult containing train and test datasets
     */
    public SplitResult splitDataset(MutableDataset<?> dataset, int trainTestSplit) {
        if (trainTestSplit < 50 || trainTestSplit > 90) {
            throw new IllegalArgumentException("Train/test split must be between 50% and 90%");
        }
        
        long seed = System.currentTimeMillis();
        return splitDataset(dataset, trainTestSplit, seed);
    }
    
    /**
     * Split dataset with custom seed for reproducibility
     * 
     * @param dataset The full dataset
     * @param trainTestSplit The percentage of data to use for training
     * @param seed Random seed for reproducibility
     * @return A SplitResult containing train and test datasets
     */
    @SuppressWarnings("unchecked")
    public SplitResult splitDataset(MutableDataset<?> dataset, int trainTestSplit, long seed) {
        if (trainTestSplit < 50 || trainTestSplit > 90) {
            throw new IllegalArgumentException("Train/test split must be between 50% and 90%");
        }
        
        double trainFraction = trainTestSplit / 100.0;
        
        log.info("Splitting dataset: {}% training, {}% testing (seed: {})", 
            trainTestSplit, 100 - trainTestSplit, seed);
        
        // Determine dataset type and create appropriate split
        MutableDataset<?> trainDataset;
        MutableDataset<?> testDataset;
        
        // Check if it's a classification or regression dataset by checking output info type
        String outputInfoClassName = dataset.getOutputInfo().getClass().getName();
        if (outputInfoClassName.contains("Label") || outputInfoClassName.contains("classification")) {
            // Classification dataset
            MutableDataset<Label> labelDataset = (MutableDataset<Label>) dataset;
            trainDataset = splitClassificationDataset(labelDataset, trainFraction, seed);
            testDataset = splitClassificationDataset(labelDataset, 1.0 - trainFraction, seed, true);
        } else {
            // Regression dataset
            MutableDataset<Regressor> regressorDataset = (MutableDataset<Regressor>) dataset;
            trainDataset = splitRegressionDataset(regressorDataset, trainFraction, seed);
            testDataset = splitRegressionDataset(regressorDataset, 1.0 - trainFraction, seed, true);
        }
        
        log.info("Split complete - Training: {} examples, Testing: {} examples", 
            trainDataset.size(), testDataset.size());
        
        return new SplitResult(trainDataset, testDataset);
    }
    
    private MutableDataset<Label> splitClassificationDataset(
            MutableDataset<Label> dataset, double fraction, long seed, boolean isTest) {
        List<Example<Label>> examples = new ArrayList<>();
        for (Example<Label> example : dataset) {
            examples.add(example);
        }
        
        Random random = new Random(seed);
        Collections.shuffle(examples, random);
        
        int splitPoint = isTest ? 
            (int) (examples.size() * (1.0 - fraction)) : 
            (int) (examples.size() * fraction);
        int endPoint = isTest ? examples.size() : splitPoint;
        int startPoint = isTest ? splitPoint : 0;
        
        // Create new dataset using the same output factory as the original
        MutableDataset<Label> result = new MutableDataset<>(
            dataset.getProvenance(),
            dataset.getOutputFactory()
        );
        for (int i = startPoint; i < endPoint; i++) {
            result.add(examples.get(i));
        }
        
        return result;
    }
    
    private MutableDataset<Label> splitClassificationDataset(
            MutableDataset<Label> dataset, double fraction, long seed) {
        return splitClassificationDataset(dataset, fraction, seed, false);
    }
    
    private MutableDataset<Regressor> splitRegressionDataset(
            MutableDataset<Regressor> dataset, double fraction, long seed, boolean isTest) {
        List<Example<Regressor>> examples = new ArrayList<>();
        for (Example<Regressor> example : dataset) {
            examples.add(example);
        }
        
        Random random = new Random(seed);
        Collections.shuffle(examples, random);
        
        int splitPoint = isTest ? 
            (int) (examples.size() * (1.0 - fraction)) : 
            (int) (examples.size() * fraction);
        int endPoint = isTest ? examples.size() : splitPoint;
        int startPoint = isTest ? splitPoint : 0;
        
        // Create new dataset using the same output factory as the original
        MutableDataset<Regressor> result = new MutableDataset<>(
            dataset.getProvenance(),
            dataset.getOutputFactory()
        );
        for (int i = startPoint; i < endPoint; i++) {
            result.add(examples.get(i));
        }
        
        return result;
    }
    
    private MutableDataset<Regressor> splitRegressionDataset(
            MutableDataset<Regressor> dataset, double fraction, long seed) {
        return splitRegressionDataset(dataset, fraction, seed, false);
    }
}
