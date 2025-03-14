from flask import Flask, request, jsonify
from rembg import remove
from PIL import Image
import joblib
import torch
import torchvision.transforms as transforms
import torchvision.models as models
import numpy as np
import io
import logging

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

app = Flask(__name__)

# Load the VGG16 model and modify it to extract features from the penultimate layer
vgg16 = models.vgg16(weights='DEFAULT')
vgg16.classifier = torch.nn.Sequential(*list(vgg16.classifier.children())[:-3])
vgg16.eval()

# Load the KNN model
knn = joblib.load('knn_model.pkl')

# Preprocessing for the image
preprocess = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
])

def extract_features(image):
    image = preprocess(image).unsqueeze(0)
    with torch.no_grad():
        features = vgg16(image).squeeze(0).detach().numpy()
    return features

def classify_image(image, top_k=3, distance_threshold=0.5):
    features = extract_features(image).reshape(1, -1)

    # Get probabilities and distances from the KNN model
    probabilities = knn.predict_proba(features)
    distances, _ = knn.kneighbors(features, n_neighbors=len(knn.classes_))

    distances = distances.flatten()

    top_k_indices = np.argsort(probabilities[0])[-top_k:][::-1]
    top_k_probabilities = probabilities[0][top_k_indices]
    top_k_classes = knn.classes_[top_k_indices]
    top_k_distances = distances[top_k_indices]

    results = []
    for cls, prob, dist in zip(top_k_classes, top_k_probabilities, top_k_distances):
        if dist < distance_threshold:
            results.append({"class": cls, "confidence": float(prob)})

    return results

@app.route('/classify', methods=['POST'])
def classify():
    try:
        # Load image from the request
        file = request.files['image']
        img = Image.open(io.BytesIO(file.read())).convert("RGB")
        img_no_bg = remove(img)
        white_bg = Image.new("RGBA", img_no_bg.size, (255, 255, 255, 255))
        img_with_white_bg = Image.alpha_composite(white_bg, img_no_bg).convert("RGB")

        # Run classification
        results = classify_image(img_with_white_bg, distance_threshold=0.1)

        # Return results as JSON
        return jsonify(results), 200

    except Exception as e:
        logging.error(f"Error occurred: {str(e)}")
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    logging.info("Python server has started.")
    app.run(host='0.0.0.0', port=5000)
