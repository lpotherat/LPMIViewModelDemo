package org.diiage.lpotherat.poc.lpmiviewmodeldemo;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.diiage.lpotherat.poc.lpmiviewmodeldemo.dal.AppDatabase;
import org.diiage.lpotherat.poc.lpmiviewmodeldemo.model.Operation;

import java.util.Objects;

public class MainActivityViewModel extends ViewModel {

    /**
     * Déclaration d'une Factory
     * Etant donné que les ViewModels sont exclusivement fournis par des ViewModelProvider,
     * il est nécessaire de créer une Factory pour pouvoir passer des paramètres au constructeur.
     *
     * Notre factory ici permet de fournir l'objet AppDatabase à notre ViewModel
     */
    public static class Factory implements ViewModelProvider.Factory{

        /**
         * L'AppDatabase à fournir
         */
        AppDatabase appDatabase;

        /**
         * Constructeur paramétré de la factory, avec l'AppDatabase à fournir
         * @param appDatabase
         */
        public Factory(AppDatabase appDatabase) {
            this.appDatabase = appDatabase;
        }

        /**
         * Cette méthode est appelée par les ViewModelProvider pour créer des instances de viewmodel.
         * Elle doit retourner une instance de viewmodel correspondant à modelClass
         * @param modelClass la classe à instancier
         * @param <T> Le type de viewmodel à retourner
         * @return le viewmodel correctement instancié
         */
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new MainActivityViewModel(appDatabase);
        }
    }

    //Type mutablelivedata, permet d'avoir un flux de données
    //bidirectionnel lecture / écriture
    //MutableLiveData<String> val1 = new MutableLiveData<>();
    //MutableLiveData<String> val2 = new MutableLiveData<>();
    // - Edit, lors de l'ajout de la base de données, on doit utiliser des MediatorLiveData car
    // l'opération issue de la base de données est une nouvelle source
    MediatorLiveData<String> val1 = new MediatorLiveData<>();
    MediatorLiveData<String> val2 = new MediatorLiveData<>();

    //Type livedata, fourni un flux unidirectionel en lecture
    LiveData<String> resultat;

    //Déclaration d'un identifiant d'opération que l'on fera bouger avec les actions
    //suivant - precedent. Dès que cet identifiant sera modifié, il déclenchera la récupération
    //de l'opération correspondante en base de données
    MutableLiveData<Long> id = new MutableLiveData<>();
    //Déclaration d'une opération, qui représentera l'opération actuellement sélectionnée par id
    LiveData<Operation> operation;

    public MainActivityViewModel(AppDatabase appDatabase) {

        //Initialisation de l'identifiant à 1 de manière arbitraire
        id.setValue(1L);

        //Utilisation de switchMap pour faire en sorte que operation soit dépendant de la valeur de id.
        // - id est une source d'information, dès que id change de valeur, getById est appelé sur le dao
        // avec la valeur de l'id en cours. Le dao retourne un LiveData contenant une Operation, qui
        // est alors observable. Dès qu'une modification a lieu dans la base de données, l'opération
        // est automatiquement mise à jour.
        operation = Transformations.switchMap(id,input -> appDatabase.operationDao().getById(input));

        //-----------------------------------------
        // on ajoute ici operation en tant que source de données pour val1 et val2
        val1.addSource(operation,op -> {
            if (op != null) {
                val1.setValue(String.valueOf(op.getVal1()));
            } else {
                val1.setValue("");
            }
        });
        val2.addSource(operation,op -> {
            if (op != null) {
                val2.setValue(String.valueOf(op.getVal2()));
            } else {
                val2.setValue("");
            }
        });
        //-----------------------------------------

        //Un mediatorlivedata permet de conciler plusieurs sources de données
        //Dans cet exemple, résultat est abonné à val1 et val2.
        //Pour chaque changement dans val1 ou val2, on affecte dans resultat
        //la somme des deux valeurs.
        resultat = new MediatorLiveData<String>(){{
            //Ajout de val1 en tant que source de données
            addSource(val1,string -> {
                try {
                    //Récupération de la valeur courante de val1
                    int intVal1 = Integer.valueOf(string);
                    //Récupération de la valeur courante de val2
                    int intVal2 = Integer.valueOf(val2.getValue());
                    //Affectation de la somme au MediatorLiveData
                    setValue(String.valueOf(intVal1 + intVal2));
                } catch (Exception ex){
                    setValue("");
                }
            });

            //Ajout de val2 en tant que source de données
            addSource(val2, string -> {
                try {
                    int intVal1 = Integer.valueOf(val1.getValue());
                    int intVal2 = Integer.valueOf(string);
                    setValue(String.valueOf(intVal1 + intVal2));
                } catch (Exception ex){
                    setValue("");
                }
            });
        }};

    }

    /**
     * Exposition du flux bidirectionnel val1 à la vue
     * @return
     */
    public MutableLiveData<String> getVal1() {
        return val1;
    }

    /**
     * Exposition du flux biidirectionnel val2 à la vue
     * @return
     */
    public MutableLiveData<String> getVal2() {
        return val2;
    }

    /**
     * Exposition d'un flux unidirectionel du résultat à la vue
     * @return
     */
    public LiveData<String> getResultat() {
        return resultat;
    }

    /**
     * Avancer vers l'opération suivante
     */
    public void next(){
        id.setValue(Objects.requireNonNull(id.getValue()) + 1);
    }

    /**
     * Retourner vers l'opération précédente
     */
    public void previous(){
        id.setValue(Objects.requireNonNull(id.getValue()) - 1);
    }
}
