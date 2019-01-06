package networkprogramming.kth.hangman;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import networkprogramming.kth.hangman.common.Message;

public class GameActivity extends Activity implements CommunicationHandler {
    private LinearLayout letters;
    private LinearLayout wordLayout;
    private LinearLayout scoreLayout;
    private EditText guess;
    private Button button;
    private ImageButton button2;
    private ServerConnection serv;
    private TextView[] charViews;
    private TextView scoreView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        wordLayout = (LinearLayout)findViewById(R.id.word);
        scoreLayout = (LinearLayout)findViewById(R.id.letters);
        letters = (LinearLayout)findViewById(R.id.letters);
        button = (Button)findViewById(R.id.button);
        button2 = (ImageButton)findViewById(R.id.imageButton);
        guess = findViewById(R.id.guess);
        new ConnectServer().execute();
        button2.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        new Thread(new Runnable(){

                            @Override
                            public void run() {
                                GameActivity.this.finish();
                            }
                        }).start();
                    }
                });
        button.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        new Thread(new Runnable(){

                            @Override
                            public void run() {
                                try {
                                    serv.sendGuess(guess.getText().toString());
                                } catch (IOException e) {
                                    quitGame();
                                    e.printStackTrace();
                                }catch (NullPointerException e){
                                    quitGame();
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                });
    }
    public void quitGame(){
        final AlertDialog.Builder errorBuild = new AlertDialog.Builder(this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                errorBuild.setTitle("Problem connecting to Server. Please try again later");

                errorBuild.setPositiveButton("Return to Main Page",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                GameActivity.this.finish();
                            }});
                errorBuild.show();
            }
        });

    }

    public void updateView(final Message mes) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                update(mes);
            }
        });

    }

    public void update(Message mes){

        if(mes.getStatus().equals("null")) {
            charViews = new TextView[mes.getWord().length];
            scoreView = new TextView(this);
            scoreLayout.removeAllViews();
            //scoreView.setText("Score: " + mes.getScore());
            TextView score = (TextView) findViewById(R.id.score);
            score.setText("Score: " + mes.getScore());
            TextView tries = (TextView) findViewById(R.id.tries);
            tries.setText("Tries: " + mes.getTries());
            scoreLayout.addView(scoreView);
            wordLayout.removeAllViews();
            guess.setText("");
            letters.addView(guess);
            char[] currentWord = mes.getWord();
            for(int c=0; c<mes.getWord().length; c++) {
                charViews[c] = new TextView(this);

                charViews[c].setText("  " + currentWord[c]);

                charViews[c].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT));
                charViews[c].setGravity(Gravity.CENTER);
                charViews[c].setTextSize(30);
                wordLayout.addView(charViews[c]);
            }
        }else{
            if(mes.getStatus().equals("true")){
                AlertDialog.Builder winBuild = new AlertDialog.Builder(this);
                winBuild.setTitle("YAY");
                winBuild.setMessage("You win!\n\nThe answer was:\n\n"+String.valueOf(mes.getWord()));
                winBuild.setPositiveButton("Play Another Round",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                startActivity(getIntent());
                            }});
                winBuild.setNegativeButton("Quit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                GameActivity.this.finish();
                            }});
                winBuild.show();
            }else{
                AlertDialog.Builder loseBuild = new AlertDialog.Builder(this);
                loseBuild.setTitle("OOPS");
                loseBuild.setMessage("You lose!\n\nThe answer was:\n\n"+String.valueOf(mes.getWord()));
                loseBuild.setPositiveButton("Play Another Round",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                startActivity(getIntent());
                            }});
                loseBuild.setNegativeButton("Quit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                GameActivity.this.finish();
                            }});
                loseBuild.show();
            }
        }
    }
    private class ConnectServer extends AsyncTask<Void, Void, ServerConnection> {
        @Override
        protected ServerConnection doInBackground(Void... _) {
            InetAddress srv_host = null;
            try {
                srv_host = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            Integer srv_port = 1337;
            serv = new ServerConnection(GameActivity.this,
                    srv_host, srv_port);
            serv.connect();
            try {
                serv.startGame();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return serv;
        }
        @Override
        protected void onPostExecute(ServerConnection srvConnection) {
            GameActivity.this.serv = srvConnection;
            new Thread(srvConnection).start();
        }
    }
}
