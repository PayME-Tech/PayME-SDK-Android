package vn.payme.sdk.cardmodules;

import android.graphics.Bitmap;

import java.util.ArrayList;

class RecognizeNumbers {

	private final RecognizedDigits[][] recognizedDigits;
	private final Bitmap image;

	RecognizeNumbers(Bitmap image, int numRows, int numCols) {
		this.image = image;
		this.recognizedDigits = new RecognizedDigits[numRows][numCols];
	}

	private boolean didStartScan = false;
  CardScanned number(RecognizedDigitsModel model, ArrayList<ArrayList<DetectedBox>> lines) {
		for (ArrayList<DetectedBox> line : lines) {
			StringBuilder candidateNumber = new StringBuilder();

			StringBuilder candidateNumberTest = new StringBuilder();

			for (DetectedBox word : line) {
				RecognizedDigits recognized = this.cachedDigits(model, word);
				if (recognized == null) {
					return null;
				}

			if (recognized.stringResult().length() >= 3) {
      					didStartScan = true;
      }
				candidateNumber.append(recognized.stringResult());
			}

			if (candidateNumber.length() >= 16) {
				return new CardScanned(candidateNumber.toString(), didStartScan);
			}
		}
		return new CardScanned(null, didStartScan);
	}

	private RecognizedDigits cachedDigits(RecognizedDigitsModel model, DetectedBox box) {
		if (this.recognizedDigits[box.row][box.col] == null) {
			this.recognizedDigits[box.row][box.col] = RecognizedDigits.from(model, image, box.getRect());
		}
		return this.recognizedDigits[box.row][box.col];
	}

}
