import joblib
from sklearn.tree import DecisionTreeClassifier
import numpy as np

# Dummy training data (same structure we used before)
X = np.array([
    [120, 5, 1, 0.9],   # Fraud
    [60, 1, 0, 0.2],    # Legit
    [240, 10, 1, 0.85], # Fraud
    [30, 2, 0, 0.1],    # Legit
    [180, 7, 1, 0.8],   # Fraud
    [45, 1, 0, 0.3]     # Legit
])
y = np.array([1, 0, 1, 0, 1, 0])  # 1 = Fraud, 0 = Legit

# Train model
model = DecisionTreeClassifier()
model.fit(X, y)

# Save the model
joblib.dump(model, "fraud_model1.pkl")
print("✅ Model trained and saved as fraud_model.pkl")
