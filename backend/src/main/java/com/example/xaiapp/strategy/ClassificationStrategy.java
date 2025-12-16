package com.example.xaiapp.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tribuo.MutableDataset;
import org.tribuo.Model;
import org.tribuo.classification.Label;
import org.tribuo.classification.sgd.linear.LogisticRegressionTrainer;

import java.util.Map;

/**
 * Classification training strategy using Logistic Regression
 * 
 * This strategy implements the TrainingStrategy interface for
 * classification tasks using Tribuo's LogisticRegressionTrainer.
 * 
 * @since 1.0.0
 */
@Component
@Slf4j
public class ClassificationStrategy implements TrainingStrategy {

    // Flag to control normalization (revertable)
    private static final boolean USE_NORMALIZATION = true;

    @Override
    public Model<?> train(MutableDataset<?> dataset, Map<String, Object> parameters) throws Exception {
        log.info("Starting classification training with Logistic Regression");

        validateDataset(dataset);

        @SuppressWarnings("unchecked")
        MutableDataset<Label> labelDataset = (MutableDataset<Label>) dataset;

        LogisticRegressionTrainer trainer = new LogisticRegressionTrainer();

        log.info("Training dataset size: {} examples", labelDataset.size());
        log.info("Number of features: {}", labelDataset.getFeatureMap().size());
        log.info("Number of classes: {}", labelDataset.getOutputInfo().size());

        Model<Label> model;
        if (USE_NORMALIZATION) {
            log.info("Normalization enabled: Applying Z-Score transformation (MeanStdDev)");
            // Create a transformation map that scales all features to zero mean and unit
            // variance
            // using MeanStdDevTransformation (Z-Score)
            org.tribuo.transform.TransformationMap transformationMap = new org.tribuo.transform.TransformationMap(
                    java.util.Collections
                            .singletonList(new org.tribuo.transform.transformations.MeanStdDevTransformation()));

            // Wrap the trainer with a TransformTrainer to automatically apply
            // transformations
            // during training and prediction
            org.tribuo.transform.TransformTrainer<Label> transformTrainer = new org.tribuo.transform.TransformTrainer<>(
                    trainer,
                    transformationMap);

            model = transformTrainer.train(labelDataset);
        } else {
            log.info("Normalization disabled: Training on raw data");
            model = trainer.train(labelDataset);
        }

        log.info("Classification training completed successfully");
        return model;
    }

    @Override
    public String getAlgorithmName() {
        return "Logistic Regression";
    }

    @Override
    public String getModelType() {
        return "CLASSIFICATION";
    }

    @Override
    public void validateDataset(MutableDataset<?> dataset) throws IllegalArgumentException {
        if (dataset == null) {
            throw new IllegalArgumentException("Dataset cannot be null");
        }

        if (dataset.size() == 0) {
            throw new IllegalArgumentException("Dataset cannot be empty");
        }

        if (dataset.getFeatureMap().size() == 0) {
            throw new IllegalArgumentException("Dataset must have at least one feature");
        }

        // Check if it's a classification dataset
        if (!(dataset instanceof MutableDataset<?>)) {
            throw new IllegalArgumentException("Dataset must be a MutableDataset for classification");
        }

        // Additional validation for classification
        @SuppressWarnings("unchecked")
        MutableDataset<Label> labelDataset = (MutableDataset<Label>) dataset;

        if (labelDataset.getOutputInfo().size() < 2) {
            throw new IllegalArgumentException("Classification requires at least 2 classes");
        }

        log.debug("Dataset validation passed for classification");
    }
}
