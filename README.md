# DecisionTreeClassifier-in-java
This repository contains an implementation of Decision Tree Classifier built from scratch in Java. Gini impurity is used to split data recursively and make predictions. It supports continuous features and works for dataset with multiple output classes.
### Features
- **Gini impurity** for splitting nodes
- **Supports multiple classes** for classification tasks
- **Predict function** for unseen data
- **Custom tree building logic** with recursive splitting
## How It Works
#### 1. Training (`fit()` method):

- Splits the data recursively by selecting the best feature and threshold based on Gini impurity.
- The recursion stops when all the data points at a node belong to the same class or no further splitting is possible.

#### 2. Prediction (`predict()` method):

- Traverses the tree from the root to a leaf node based on input feature values.
- Returns the label stored in the leaf node as the prediction.

#### 3. Handling Edge Cases:

- Checks if input data matches the number of features in the tree.
- Ensures only valid Double inputs are processed.
