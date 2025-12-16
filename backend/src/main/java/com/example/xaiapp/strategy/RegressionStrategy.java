package com.example.xaiapp.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tribuo.MutableDataset;
import org.tribuo.Model;
import org.tribuo.Trainer;
import org.tribuo.regression.Regressor;
import org.tribuo.regression.sgd.linear.LinearSGDTrainer;
import org.tribuo.regression.sgd.objectives.SquaredLoss;
import org.tribuo.math.optimisers.AdaGrad;
import com.example.xaiapp.config.MLTrainingConfig;

import java.util.Map;

/**
 * Regression training strategy using Linear SGD
 * 
 * This strategy implements the TrainingStrategy interface for
 * regression tasks using Tribuo's LinearSGDTrainer.
 * 
 * @since 1.0.0
 */
@Component
@Slf4j
public class RegressionStrategy implements TrainingStrategy {

    // Manual log field (Lombok @Slf4j not generating it)
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegressionStrategy.class);

    private final MLTrainingConfig mlConfig;

    public RegressionStrategy(MLTrainingConfig mlConfig) {
        this.mlConfig = mlConfig;
    }

    // Flag to control normalization (revertable)
    private static final boolean USE_NORMALIZATION = true;

    @Override
    public Model<?> train(MutableDataset<?> dataset, Map<String, Object> parameters) throws Exception {
        log.info("Starting regression training with Linear SGD");

        validateDataset(dataset);

        @SuppressWarnings("unchecked")
        MutableDataset<Regressor> regressorDataset = (MutableDataset<Regressor>) dataset;

        // Configure LinearSGDTrainer with configurable parameters
        LinearSGDTrainer trainer = new LinearSGDTrainer(
                new SquaredLoss(),
                new AdaGrad(mlConfig.getRegression().getLearningRate(),
                        mlConfig.getRegression().getInitialLearningRate()),
                mlConfig.getRegression().getEpochs(),
                1000, // logging interval
                mlConfig.getRegression().getMinibatchSize(),
                Trainer.DEFAULT_SEED);

        log.info("Training dataset size: {} examples", regressorDataset.size());
        log.info("Number of features: {}", regressorDataset.getFeatureMap().size());

        Model<Regressor> model;
        if (USE_NORMALIZATION) {
            log.info("Normalization enabled: Applying Z-Score transformation (MeanStdDev)");
            // Create a transformation map that scales all features to zero mean and unit
            // variance
            org.tribuo.transform.TransformationMap transformationMap = new org.tribuo.transform.TransformationMap(
                    java.util.Collections
                            .singletonList(new org.tribuo.transform.transformations.MeanStdDevTransformation()));

            // Wrap the trainer with a TransformTrainer to automatically apply
            // transformations
            // during training and prediction
            org.tribuo.transform.TransformTrainer<Regressor> transformTrainer = new org.tribuo.transform.TransformTrainer<>(
                    trainer,
                    transformationMap);

            model = transformTrainer.train(regressorDataset);
        } else {
            log.info("Normalization disabled: Training on raw data");
            model = trainer.train(regressorDataset);
        }

        log.info("Regression training completed successfully");
        return model;
    }

    @Override
    public String getAlgorithmName() {
        return "Linear SGD";
    }

    @Override
    public String getModelType() {
        return "REGRESSION";
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

        // Check if it's a regression dataset
        if (!(dataset instanceof MutableDataset<?>)) {
            throw new IllegalArgumentException("Dataset must be a MutableDataset for regression");
        }

        // Additional validation for regression
        @SuppressWarnings("unchecked")
        MutableDataset<Regressor> regressorDataset = (MutableDataset<Regressor>) dataset;

        if (regressorDataset.getOutputInfo().size() == 0) {
            throw new IllegalArgumentException("Regression requires at least one output variable");
        }

        log.debug("Dataset validation passed for regression");
    }
}
