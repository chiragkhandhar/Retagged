package ml.chiragkhandhar.retagged;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class ExploreActivity extends AppCompatActivity implements View.OnClickListener {
    RecyclerView rv;
    ArrayList<Double> ll;
    double latitude, longitude;
    private ArrayList<Explore> exploreArrayList = new ArrayList<>();
    private static final String TAG = "ExploreActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        setupComponents();
        new ExploreLoader(ExploreActivity.this).execute(ll);
    }

    void setupComponents() {
        rv = findViewById(R.id.recycler);
        ll = new ArrayList<>();
        ll.add(getIntent().getExtras().getDouble("latitude"));
        ll.add(getIntent().getExtras().getDouble("longitude"));
    }

    void updateRecycler(ArrayList<Explore> tempList) {

    }

    @Override
    public void onClick(View view) {
        int position = rv.getChildAdapterPosition(view);
        Explore temp = exploreArrayList.get(position);

//        Intent i = new Intent(this,ExploreActivity.class);
//        startActivity(i);
        Toast.makeText(this, "Selected " + temp.getName(), Toast.LENGTH_SHORT).show();


    }
}
