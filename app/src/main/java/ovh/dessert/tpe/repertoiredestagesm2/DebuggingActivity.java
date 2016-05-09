package ovh.dessert.tpe.repertoiredestagesm2;

import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.opencsv.CSVReader;

import java.util.List;

import ovh.dessert.tpe.repertoiredestagesm2.entities.Entreprise;

public class DebuggingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debugging);
    }

    public void initDB(View v) {
        final TextView textViewToChange = (TextView) findViewById(R.id.textView);
        try {
            StagesDAO test = StagesDAO.getInstance(getApplicationContext());
            //test.update(StagesDAO.UpdateContext.OFFLINE, getApplicationContext());
            List<Entreprise> ent = test.getAllEntreprises();
            textViewToChange.setText("C'est coule : " + ent.size());
        }catch(Exception e) {
            textViewToChange.setText("C'est pas coule.");
            Log.d("initdb", e.toString());
        }
    }
}