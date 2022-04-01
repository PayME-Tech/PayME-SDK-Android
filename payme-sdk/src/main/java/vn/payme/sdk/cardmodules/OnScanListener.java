package vn.payme.sdk.cardmodules;

interface OnScanListener {
	void onPrediction(final CardScanned cardScanned);
  void onFatalError();
}

class CardScanned {
	public String number;
	public boolean didStartScan;

	public CardScanned(String num, boolean didStart) {
		number = num;
		didStartScan = didStart;
	}
}
