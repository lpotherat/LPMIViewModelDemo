package org.diiage.lpotherat.poc.lpmiviewmodeldemo;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import android.view.View;

import org.diiage.lpotherat.poc.lpmiviewmodeldemo.dal.AppDatabase;
import org.diiage.lpotherat.poc.lpmiviewmodeldemo.databinding.ActivityMainBinding;
import org.diiage.lpotherat.poc.lpmiviewmodeldemo.model.Operation;

import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialisation de la base de données.
        //Note : Ce n'est pas le meilleur endroit pour le faire, le mieux
        //serait d'utiliser Dagger2 pour injecter la dépendance.
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "operations.sqlite").build();


        /*
        // Code d'initialisation des données, ne doit être executé qu'une seule fois.
        Executors.newSingleThreadExecutor().execute(() -> {
            db.operationDao().insert(new Operation(0,1,1));
            db.operationDao().insert(new Operation(0,2,2));
            db.operationDao().insert(new Operation(0,3,3));
            db.operationDao().insert(new Operation(0,420,69));
        });*/



        //Récupération d'une instance de notre viewmodel
        //On utilise un ViewModelProvider, jamais directement "new".
        MainActivityViewModel viewModel =
        //On indique au provider que this est le StoreOwner
                new ViewModelProvider(this,new MainActivityViewModel.Factory(db))
        //On lui demande ensuite une instance de notre ViewModel
                        .get(MainActivityViewModel.class);

        //Création de la vue avec DataBindingUtil pour pouvoir récupérer
        //toutes les informations du binding, notamment le fait que l'on
        //puisse transmettre le viewmodel
        ActivityMainBinding binding =
                DataBindingUtil.setContentView(this
                        ,R.layout.activity_main);

        //Affectation de this en tant que LifecycleOwner, cela permet au
        //binding d'observer les LiveData du viewmodel
        binding.setLifecycleOwner(this);

        //Affectation du viewmodel au binding
        binding.setViewModel(viewModel);


        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });




    }

}
