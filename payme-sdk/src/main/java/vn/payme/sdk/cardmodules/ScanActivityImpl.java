package vn.payme.sdk.cardmodules;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import vn.payme.sdk.R;

public class ScanActivityImpl extends ScanBaseActivity {

    private static final String TAG = "ScanActivityImpl";

    public static final String SCAN_CARD_TEXT = "scanCardText";
    public static final String POSITION_CARD_TEXT = "positionCardText";

    public static final String RESULT_CARD_NUMBER = "cardNumber";
    public static final String RESULT_EXPIRY_MONTH = "expiryMonth";
    public static final String RESULT_EXPIRY_YEAR = "expiryYear";

    public static final String RESULT_VALID_DATE = "validDate";
    public static final String RESULT_EXPIRY_DATE = "expiryDate";

    private ImageView mDebugImageView;
    private String cardNumber = null;
    private boolean mInDebugMode = false;

    TextRecognizer recognizer =
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_card);

        String scanCardText = getIntent().getStringExtra(SCAN_CARD_TEXT);
//		if (!TextUtils.isEmpty(scanCardText)) {
//			((TextView) findViewById(R.id.scanCard)).setText(scanCardText);
//		}
//
//		String positionCardText = getIntent().getStringExtra(POSITION_CARD_TEXT);
//		if (!TextUtils.isEmpty(positionCardText)) {
//			((TextView) findViewById(R.id.positionCard)).setText(positionCardText);
//		}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 110);
            } else {
                mIsPermissionCheckDone = true;
            }
        } else {
            // no permission checks
            mIsPermissionCheckDone = true;
        }

        findViewById(R.id.closeButton).setOnClickListener(v -> {
            if (cardNumber != null) {
                Intent intent = new Intent();
                intent.putExtra(RESULT_CARD_NUMBER, cardNumber);
                setResult(RESULT_OK, intent);
            }
            finish();
        });

        mDebugImageView = findViewById(R.id.debugImageView);
        mInDebugMode = getIntent().getBooleanExtra("debug", false);
        if (!mInDebugMode) {
            mDebugImageView.setVisibility(View.INVISIBLE);
        }
        setViewIds(R.id.cardRectangle, R.id.shadedBackground, R.id.texture,
                R.id.cardNumber, R.id.expiry);
    }

    @Override
    protected void onPause() {
        onBackPressed();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (cardNumber != null) {
            Intent intent = new Intent();
            intent.putExtra(RESULT_CARD_NUMBER, cardNumber);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCardScanned(String numberResult, String month, String year) {
        cardNumber = numberResult;
        Intent intent = new Intent();
                intent.putExtra(RESULT_CARD_NUMBER, cardNumber);
                setResult(RESULT_OK, intent);
        finish();

//		finish();
    }

    private final HashMap<String, Integer> dateFrequency = new HashMap<>();
    private Integer count = 0;

    @Override
     public void onPrediction(final CardScanned cardScanned) {
        super.onPrediction(cardScanned);
     }

    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm) {
        List<Map.Entry<String, Integer> > list = new LinkedList<>(hm.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        HashMap<String, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

}
