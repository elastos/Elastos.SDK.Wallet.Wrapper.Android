package org.elastos.sdk.wallet.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.elastos.sdk.wallet.BlockChainNode;
import org.elastos.sdk.wallet.Did;
import org.elastos.sdk.wallet.DidManager;
import org.elastos.sdk.wallet.HDWallet;
import org.elastos.sdk.wallet.Identity;
import org.elastos.sdk.wallet.IdentityManager;
import org.elastos.sdk.wallet.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import static org.elastos.sdk.wallet.IdentityManager.getMnemonic;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onStart() {
        super.onStart();

        TextView txtMsg = findViewById(R.id.txt_message);

        findViewById(R.id.btn_test_mnemonic).setOnClickListener((view) -> {
            String message = testGenrateMnemonic();
            txtMsg.setText(message);
        });
        findViewById(R.id.btn_test_singlewallet).setOnClickListener((view) -> {
            testSingleWallet();
        });
        findViewById(R.id.btn_test_hdwallet).setOnClickListener((view) -> {
            testHDWalletAddress();
        });
        findViewById(R.id.btn_test_did).setOnClickListener((view) -> {
            String message = testDid();
            txtMsg.setText(message);
        });
        findViewById(R.id.btn_test_sign).setOnClickListener((view) -> {
            String message = testSignTxData();
            message += testCosignTxData();
            txtMsg.setText(message);
        });
    }

    private String readWords() {
        StringBuilder buf = new StringBuilder();
        InputStream file = null;
        try {
            file = getAssets().open("mnemonic_chinese.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(file, "UTF-8"));

            String str;

            while ((str = in.readLine()) != null) {
                buf.append(str);
                buf.append('\n');
            }

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Log.d("Test", buf.toString());
        return buf.toString();
    }

    private String testGenrateMnemonic() {
        return IdentityManager.getMnemonic("chinese", readWords());
    }

    private void testSingleWallet() {
        String mnemonic = "血 坝 告 售 代 讨 转 枝 欧 旦 诚 抱";
        String seed = IdentityManager.getSeed(mnemonic, "");
        Log.d("Test", "seed: " + seed);

        Identity identity = IdentityManager.createIdentity(getFilesDir().getAbsolutePath());

        BlockChainNode node = new BlockChainNode("https://api-wallet-ela-testnet.elastos.org");
        HDWallet singleWallet = identity.createSingleAddressWallet(seed, node);

        singleWallet.syncHistory();

        String address = singleWallet.getAddress(0, 0);
        Log.d("Test", "addr: " + address);

        long balance = singleWallet.getBalance("EdVgb5RTdmwKf79pEUdVNnFprWyvmr1hPc");
        Log.d("Test", "balance: " + balance);
    }

    private void testHDWalletAddress() {
        String mnemonic = "血 坝 告 售 代 讨 转 枝 欧 旦 诚 抱";
        String seed = IdentityManager.getSeed(mnemonic, "");
        Log.d("Test", "seed: " + seed);

        Identity identity = IdentityManager.createIdentity(getFilesDir().getAbsolutePath());

        BlockChainNode node = new BlockChainNode("https://api-wallet-ela-testnet.elastos.org");
        HDWallet wallet = identity.createWallet(seed, 0, node);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                wallet.recover();
            }
        });
//        thread.start();

        String[] usedAddrs = wallet.getUsedAddresses();
        Log.d("Test", "used addresses: ");
        for (String addr : usedAddrs) {
            Log.d("Test", " " + addr);
        }

        String[] unusedAddrs = wallet.getUnUsedAddresses(5);
        Log.d("Test", "new addresses: ");
        for (String addr : unusedAddrs) {
            Log.d("Test", " " + addr);
        }

        Transaction tx = new Transaction("ENvWkDwYdt1m5F2Pi7WEVDXk89T8qne6jj", 100000000L, 0);
        Transaction[] txs = new Transaction[1];
        txs[0] = tx;

        String txid = wallet.sendTransaction(txs, "test in java", seed, "");
        Log.d("Test", "txid: " + txid);
    }

    private String testDid() {
        String mnemonic = "血 坝 告 售 代 讨 转 枝 欧 旦 诚 抱";
        String seed = IdentityManager.getSeed(mnemonic, "");

        Identity identity = IdentityManager.createIdentity(getFilesDir().getAbsolutePath());
        BlockChainNode node = new BlockChainNode("https://api-wallet-did-testnet.elastos.org");
        HDWallet singleWallet = identity.createSingleAddressWallet(seed, node);

        singleWallet.syncHistory();

        long balance = singleWallet.getBalance();
        Log.d("Test", "balance: " + balance);

        DidManager manager = identity.createDidManager(seed);

        Did did = manager.createDid(0);

        did.setNode(node);

        String json = "[{\"Key\": \"Phone\", \"Value\":\"17699887989\"}]";
        String info = did.signInfo(seed, json, true);
        Log.d("Test", "signed: " + info);

        String txid = did.setInfo(seed, json, singleWallet, true);
        Log.d("Test", "set did info: " + txid);

        did.syncInfo();

        String value = did.getInfo("Phone", true, seed);
        Log.d("Test", "Phone: " + value);

        return "";
    }

    private String testSignTxData() {
        String message = "";

        message += "================================================\n";
        return message;
    }

    private String testCosignTxData() {
        String message = "";

        message += "================================================\n";
        return message;
    }
}
