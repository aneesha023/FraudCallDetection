# 🚨 Fraud Call Detection System

## 🌟 Project Overview

The Fraud Call Detection System is a full-stack, AI/ML-powered application designed to combat fraudulent voice calls in real-time. This project follows strong **software engineering principles** and utilizes a **Client-Server architecture** where a mobile application communicates with a powerful Python backend to perform advanced threat analysis.

The system integrates multiple detection methods—including **Natural Language Processing (NLP)** and external API checks—to deliver highly accurate, proactive security. The architecture and experimental outcomes of this work were **documented and presented in a peer-reviewed paper on fraud detection**.

**Role:** Team Lead (6 members)

## ✨ Key Features & Functionality

* **Mobile Interface:** A dedicated mobile client built on **Android Studio** for user interaction and audio input.
* **Intelligent Analysis:** Integrates **voice-to-text NLP** and **real-time audio analysis** on the backend to assess conversational risk.
* **External Validation:** Utilizes a **number-based fraud detection API** for cross-referencing and validating against known fraudulent numbers.
* **Scalable Backend:** Designed with a robust **Python** backend for efficient data processing and communication with the mobile client.

## 📊 Results and Impact

The system's innovative, multi-layered approach yielded significant performance gains:

* **Accuracy Improvement:** The solution improved fraud detection accuracy by **15% compared to baseline models**.
* **Research Publication:** The project's findings were documented and **presented in a peer-reviewed paper on fraud detection**.

## ⚙️ Technology Stack & Architecture

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Mobile Client** | **Android Studio (Java/Kotlin)** | User Interface, real-time audio capture, and display of detection results. |
| **Core Backend** | **Python** | Hosting the ML model, executing NLP, and managing the number validation API. |
| **Data Science** | AI/ML Algorithms, NLP | Core technologies driving the detection logic. |
| **Communication** | REST APIs | Used for data exchange between the Android client and the Python backend server. |

## 🚀 Getting Started (Installation)

Running this project requires setting up both the Python Backend Server and the Android Mobile Client.

### 1. Backend Server Setup (Python)

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/aneesha023/FraudCallDetection.git](https://github.com/aneesha023/FraudCallDetection.git)
    cd FraudCallDetection/backend # (Assuming the Python server is in a 'backend' folder)
    ```
2.  **Create and activate a virtual environment:**
    ```bash
    python3 -m venv venv
    source venv/bin/activate  # On Linux/macOS
    # venv\Scripts\activate  # On Windows
    ```
3.  **Install dependencies:**
    ```bash
    pip install -r requirements.txt
    ```
4.  **Configure API Key:**
    *You must set your environment variable for the external fraud detection API key.*
    ```bash
    export FRAUD_API_KEY='YOUR_KEY_HERE'
    ```
5.  **Start the server:**
    ```bash
    python server.py
    ```

### 2. Mobile Client Setup (Android Studio)

1.  **Open Project:** Open the project's root folder in **Android Studio**.
2.  **Update Endpoint:** Navigate to the client's API configuration file (e.g., `Constants.java` or `Config.kt`) and set the base URL to your running Python backend (e.g., `http://10.0.2.2:5000` for the local emulator).
3.  **Build and Run:** Build the project and run it on an Android emulator or a physical device.
