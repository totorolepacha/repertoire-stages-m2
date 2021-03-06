package ovh.dessert.tpe.repertoiredestagesm2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ovh.dessert.tpe.repertoiredestagesm2.entities.Entreprise;
import ovh.dessert.tpe.repertoiredestagesm2.entities.Localisation;
import ovh.dessert.tpe.repertoiredestagesm2.entities.Stagiaire;
import ovh.dessert.tpe.repertoiredestagesm2.exceptions.InvalidCSVException;

public class StagesDAO extends SQLiteOpenHelper {

    private static StagesDAO db = null;

    private static final String DATABASE_NAME = "repertoire.db";
    private static final int DATABASE_VERSION = 3;

    private StagesDAO(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Méthode retournant une liste de toutes les entreprises existantes en base de données
     * @return Une List d'objets Entreprise
     * @throws Exception Renvoyée si une erreur SQL a lieu
     */
    public List<Entreprise> getAllEntreprises() throws Exception {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Entreprise> retour = new ArrayList<>();

        Cursor results = db.rawQuery("SELECT * FROM Entreprise", null);
        try {
            if (results.moveToFirst()) {
                do {
                    Entreprise temp = new Entreprise(results);
                    retour.add(temp);
                }while(results.moveToNext());
            }
        } catch(Exception e) {
            throw new Exception("Erreur lors de l'éxecution de la requête.");
        } finally {
            if (results != null && !results.isClosed())
                results.close();
        }

        return retour;
    }

    /**
     * Méthode retournant une liste de toutes les localisations existantes en base de données
     * @return Une List d'objets Localisation
     * @throws Exception Renvoyée si une erreur SQL a lieu
     */
    public List<Localisation> getAllLocalisations() throws Exception {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Localisation> retour = new ArrayList<>();

        Cursor results = db.rawQuery("SELECT * FROM Localisation", null);
        try {
            if (results.moveToFirst()) {
                do {
                    Localisation temp = new Localisation(results);
                    retour.add(temp);
                }while(results.moveToNext());
            }
        } catch(Exception e) {
            throw new Exception("Erreur lors de l'éxecution de la requête.");
        } finally {
            if (results != null && !results.isClosed())
                results.close();
        }

        return retour;
    }

    /**
     * Retourne l'objet Stagiaire concernant le Stagiaire avec le login renseigné
     * @param login Le login de Stagiaire recherché
     * @return Le Stagiaire concerné
     * @throws Exception Si une erreur SQL a lieu, ou qu'aucun Stagiaire correspondant n'a été trouvé
     */
    public Stagiaire getStagiaire(String login) throws Exception {
        SQLiteDatabase db = this.getReadableDatabase();
        Stagiaire retour;

        Cursor results = db.rawQuery("SELECT * FROM Stagiaire WHERE login= ?", new String[]{login});
        try {
            if (results.moveToFirst())
                retour = new Stagiaire(results);
            else
                throw new Exception("Stagiaire inexistant.");

        } catch(Exception e) {
            throw new Exception("Erreur lors de l'éxecution de la requête.");
        } finally {
            if (results != null && !results.isClosed()) {
                results.close();
            }
        }

        return retour;
    }

    /**
     * Retourne l'objet Entreprise concernant l'Entreprise avec l'abbréviation renseignée
     * @param abbr L'abbréviation de l'Entreprise recherchée
     * @return L'Entreprise concernée
     * @throws Exception Si une erreur SQL a lieu, ou qu'aucune Entreprise correspondante n'a été trouvée
     */
    public Entreprise getEntreprise(String abbr) throws Exception {
        SQLiteDatabase db = this.getReadableDatabase();
        Entreprise retour;

        Cursor results = db.rawQuery("SELECT * FROM Entreprise WHERE abbr = ?", new String[]{abbr});
        try {
            if (results.moveToFirst())
                retour = new Entreprise(results);
            else
                throw new Exception("Entreprise non existante.");

        } catch(Exception e) {
            throw new Exception("Erreur lors de l'éxecution de la requête.");
        } finally {
            if (results != null && !results.isClosed()) {
                results.close();
            }
        }

        return retour;
    }

    /**
     * Méthode recherchant une entreprise avec les éléments renseignés.
     * Si, parmi les chaînes de recherche, une chaine est vide, son critère de recherche est omis.
     * Ex: Si le nom de l'entreprise est vide, aucun critère sur le nom de l'entreprise ne sera fait.
     * @param context Le contexte de l'activité appelant la recherche
     * @param local Le pointeur sur l'ArrayList à remplir des localisation concernés
     * @param nom Le filtre sur le nom de l'entreprise
     * @param rayon Le filtre sur le rayon de recherche
     * @param ville La ville du centre du rayon de recherche
     * @param tags Les mots-clés à filtrer.
     * @return La List des Entreprises répondant aux critères
     * @throws Exception Si une erreur SQL a lieu.
     */
    public List<Entreprise> searchEntreprises(Context context, ArrayList<Localisation> local, String nom, String rayon, String ville, String tags) throws Exception{
        SQLiteDatabase db = this.getReadableDatabase();
        List<Entreprise> retour = new ArrayList<>();

        // Génération de la requête SQL de recherche

        String requete = "SELECT * FROM Entreprise";

        if(!nom.isEmpty() || !tags.isEmpty()) {
            requete += " WHERE ";
            // Si le filtre sur le nom n'est pas vide, on filtre selon ce nom
            if(!nom.isEmpty()) {
                requete += "nom_entreprise LIKE '%" + nom.replace("!", "!!").replace("%", "!%").replace("_", "!_").replace("[", "![").replace("'", "!'") + "%'";
                if (!tags.isEmpty())
                    requete += " AND ";
            }

            // Si le champ des tags n'est pas vide, on fait un critère de sélection pour chaque tag
            if(!tags.isEmpty()) {
                requete += "abbr IN(SELECT DISTINCT entreprise FROM Stage WHERE ";
                for(String tag:tags.split(";")) {
                    requete += "mots_cles LIKE '%" + tag.replace("!", "!!").replace("%", "!%").replace("_", "!_").replace("[", "![").replace("'", "!'") + "%' AND ";
                }
                requete = requete.substring(0, requete.length() - 5) + " ESCAPE '!' )";
            }
        }

        // Nous préparons les résulats avec la requête SQL générée
        Cursor results = db.rawQuery(requete, null);

        try {
            // S'il y a au moins un résultat, on continue
            if (results.moveToFirst()) {
                do {
                    Entreprise temp = new Entreprise(results);

                    // Si la ville a été renseignée, nous filtrons le rayon.
                    if(!ville.isEmpty()) {
                        // Nous récupérons le rayon
                        int distance = Integer.parseInt(rayon.split(" ")[0]);
                        int i = 0;
                        // Puis nous récupérons, via les API Google Maps, la latitude et la longitude de la ville renseignée
                        Geocoder geo = new Geocoder(context);
                        List<Address> list = geo.getFromLocationName(ville, 1);

                        // Si une localisation à la ville a été trouvée, nous continuons
                        if (list.size() > 0) {
                            for(Localisation loc:temp.getLocalisations()) {
                                // Si la localisation correspond au rayon de recherche, nous renvoyons son entreprise, et la localisation.
                                if(Localisation.distance(list.get(0).getLatitude(), loc.getLatitude(), list.get(0).getLongitude(), loc.getLongitude()) <= distance) {
                                    if(i++ == 0)
                                        retour.add(temp);

                                    local.add(loc);
                                }
                            }
                        }
                    } else {
                        retour.add(temp);
                        local.addAll(temp.getLocalisations());
                    }
                }while(results.moveToNext());
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw new Exception("Erreur lors de l'éxecution de la requête.");
        } finally {
            if (results != null && !results.isClosed())
                results.close();
        }

        return retour;

    }

    /**
     * Permet de ne compter qu'une seule instance de base de données.
     * Cette méthode est l'unique moyen d'accéder à la base de données, afin de ne garder qu'une instance.
     * @param context Le contexte de l'application, nécéssaire à la création de la connexion à la BDD
     * @return L'objet StagesDAO permettant donc d'établir une connexion avec la BDD.
     */
    public static synchronized StagesDAO getInstance(Context context) {
        if (db == null)
            db = new StagesDAO(context.getApplicationContext());

        return db;
    }

    /**
     * Méthode lisant l'objet CSVReader toutes les entreprises, afin de les convertir dans la base de données.
     * Selon l'argument getLatLng, la méthode peut déduire les latitudes/longitudes des localisations ne possédant qu'une adresse, et
     * pas de coordonnées.
     * @param reader Le CSV dans lequel lire les informations
     * @param db L'objet de base de données dans lequel il faut écrire les informations
     * @param getLatLng booléen spécifiant s'il faut déduire les coordonnées.
     * @param context Le contexte de l'application, nécéssaire au géocoder.
     * @throws Exception
     */
    private void readEntreprise(CSVReader reader, SQLiteDatabase db, boolean getLatLng, Context context) throws Exception {
        int i = 1;
        String[] nextLine;
        String fichier = "entreprise.csv";

        while((nextLine = reader.readNext()) != null) {
            if(nextLine.length % 4 != 3) // Si la taille du fichier est invalide
                throw new InvalidCSVException(InvalidCSVException.Cause.LONGUEUR_INVALIDE, "Fichier " + fichier + " ligne " + i);
            ContentValues toInsert = new ContentValues();
            putIfNull(toInsert,"nom_entreprise", nextLine[0]);
            putIfNull(toInsert,"site_web", nextLine[1]);
            putIfNull(toInsert,"abbr", nextLine[2]);
            db.insertOrThrow("entreprise", null, toInsert);

            for (int j = 3; j < nextLine.length; j+=4) {
                if(nextLine[j].isEmpty()) continue; // S'il n'y a pas de nom, on skip
                toInsert = new ContentValues();
                putIfNull(toInsert,"nom", nextLine[j]);
                if(getLatLng)
                    if(nextLine[j+1].isEmpty() && nextLine[j+2].isEmpty() && !nextLine[j+3].isEmpty()) {
                        Geocoder geo = new Geocoder(context);
                        List<Address> list = geo.getFromLocationName(nextLine[j+3], 1);
                        if(list.size() > 0) {
                            Address temp = list.get(0);
                            nextLine[j+1] = Double.toString(temp.getLatitude());
                            nextLine[j+2] = Double.toString(temp.getLongitude());
                            Log.d("chocolat", temp.toString());
                        }
                    }

                putIfNull(toInsert,"latitude", nextLine[j+1]);
                putIfNull(toInsert,"longitude", nextLine[j+2]);
                putIfNull(toInsert,"adresse", nextLine[j+3]);
                putIfNull(toInsert,"entreprise", nextLine[2]);
                db.insertOrThrow("Localisation", null, toInsert);
            }
            i++;
        }
    }

    /**
     * Méthode lisant les stagiaires dans le CSVReader donné
     * @param reader Le CSVReader dans lequel lire les informations
     * @param db L'instance de base de données dans laquelle écrire les informations
     * @throws Exception Renvoyée si le CSV est invalide, où si une erreur SQL apparaît
     */
    private void readStagiaire(CSVReader reader, SQLiteDatabase db) throws Exception {
        String fichier = "stagiaire.csv";
        String[] nextLine;
        int i = 1;

        while((nextLine = reader.readNext()) != null) {
            if (nextLine.length != 8) // Si la taille du fichier est invalide
                throw new InvalidCSVException(InvalidCSVException.Cause.LONGUEUR_INVALIDE, "Fichier " + fichier + " ligne " + i);

            ContentValues toInsert = new ContentValues();

            putIfNull(toInsert,"nom", nextLine[0]);
            putIfNull(toInsert,"prenom", nextLine[1]);
            putIfNull(toInsert,"login", nextLine[2]);
            putIfNull(toInsert,"promotion", nextLine[3]);
            putIfNull(toInsert,"mail", nextLine[4]);
            putIfNull(toInsert,"tel", nextLine[5]);

            db.insertOrThrow("Stagiaire", null, toInsert);

            if(!nextLine[6].isEmpty() && !nextLine[7].isEmpty()) {
                toInsert = new ContentValues();
                putIfNull(toInsert,"entreprise", nextLine[6]);
                putIfNull(toInsert,"poste", nextLine[7]);
                putIfNull(toInsert,"stagiaire", nextLine[2]);

                db.insertOrThrow("Emploi", null, toInsert);
            }
            i++;
        }
    }

    /**
     * Méthode lisant les Stage dans le CSVReader donné
     * @param reader Le CSVReader dans lequel lire les informations
     * @param db L'instance de base de données dans laquelle écrire les informations
     * @throws Exception Renvoyée si le CSV est invalide, où si une erreur SQL apparaît
     */
    private void readStage(CSVReader reader, SQLiteDatabase db) throws Exception {
        String fichier = "stage.csv";
        String[] nextLine;
        int i = 1;

        while((nextLine = reader.readNext()) != null) {
            if(nextLine.length != 9) // Si la taille du fichier est invalide
                throw new InvalidCSVException(InvalidCSVException.Cause.LONGUEUR_INVALIDE, "Fichier " + fichier + " ligne " + i);

            try {
                SimpleDateFormat test = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);

                Date debut = new Date(test.parse(nextLine[3]).getTime());
                Date fin = new Date(test.parse(nextLine[4]).getTime());

                ContentValues toInsert = new ContentValues();
                putIfNull(toInsert,"sujet", nextLine[0]);
                putIfNull(toInsert,"mots_cles", nextLine[1]);
                putIfNull(toInsert,"lien_rapport", nextLine[2]);
                putIfNull(toInsert,"date_debut", debut.toString());
                putIfNull(toInsert,"date_fin", fin.toString());
                putIfNull(toInsert,"nom_maitre_stage", nextLine[5]);
                putIfNull(toInsert,"nom_tuteur_stage", nextLine[6]);
                putIfNull(toInsert,"stagiaire", nextLine[7]);
                putIfNull(toInsert,"entreprise", nextLine[8]);

                db.insertOrThrow("Stage", null, toInsert);
            } catch(ParseException pe) {
                throw new InvalidCSVException(InvalidCSVException.Cause.VALEUR_INVALIDE, "Fichier stage.csv ligne " + i + " : Date invalide (jj/MM/aaaa)");
            }
            i++;
        }
    }

    /**
     * Méthode lisant les contacts dans le CSVReader donné
     * @param reader Le CSVReader dans lequel lire les informations
     * @param db L'instance de base de données dans laquelle écrire les informations
     * @throws Exception Renvoyée si le CSV est invalide, où si une erreur SQL apparaît
     */
    private void readContact(CSVReader reader, SQLiteDatabase db) throws Exception {
        String fichier = "contact.csv";
        String[] nextLine;
        int i = 1;

        while ((nextLine = reader.readNext()) != null) {
            if (nextLine.length != 7) // Si la taille du fichier est invalide
                throw new InvalidCSVException(InvalidCSVException.Cause.LONGUEUR_INVALIDE, "Fichier " + fichier + " ligne " + i);

            ContentValues toInsert = new ContentValues();
            int civ;
            if (nextLine[0].toLowerCase().equals("monsieur")) civ = 0;
            else if (nextLine[0].toLowerCase().equals("madame")) civ = 1;
            else throw new InvalidCSVException(InvalidCSVException.Cause.VALEUR_INVALIDE, "Ligne " + i + ", civilité invalide");

            toInsert.put("civilite", civ);
            putIfNull(toInsert,"nom", nextLine[1]);
            putIfNull(toInsert,"prenom", nextLine[2]);
            putIfNull(toInsert,"entreprise", nextLine[3]);
            putIfNull(toInsert,"telephone", nextLine[4]);
            putIfNull(toInsert,"mail", nextLine[5]);
            putIfNull(toInsert,"poste", nextLine[6]);

            db.insertOrThrow("Contact", null, toInsert);

            i++;
        }
    }

    /**
     * Méthode mettant à jour la base de données avec les CSVReader préalablement remplis des
     * 4 fichiers CSV nécéssaires au remplissage de la base de données : entreprise.csv, stagiaire.csv, stage.csv, contact.csv
     * @param entrepriseReader CSVReader rempli par entreprise.csv
     * @param stagiaireReader CSVReader rempli par stagiaire.csv
     * @param stageReader CSVReader rempli par stage.csv
     * @param contactReader CSVReader rempli par contact.csv
     * @param getLatLng Booléen spécifiant si nous devons déduire les coordonées par l'adresse
     * @param context Le contexte de l'application
     * @throws Exception Renvoyée si le CSV est invalide, où si une erreur SQL apparaît
     */
    public void update(CSVReader entrepriseReader, CSVReader stagiaireReader, CSVReader stageReader, CSVReader contactReader, boolean getLatLng, Context context) throws Exception {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            this.reinit(db);
            this.onCreate(db);
            // On initialise les entreprises
            this.readEntreprise(entrepriseReader, db, getLatLng, context);
            this.readStagiaire(stagiaireReader, db);
            this.readStage(stageReader, db);
            this.readContact(contactReader, db);
            db.setTransactionSuccessful();
        }catch(SQLiteException sqe) {
            throw new InvalidCSVException(InvalidCSVException.Cause.REFERENCE_INVALIDE, sqe.getLocalizedMessage());
        }catch (IOException ioe){
            throw new InvalidCSVException(InvalidCSVException.Cause.VALEUR_INVALIDE, "");
        }finally {
            db.endTransaction();
            db.close();
        }
    }

    /**
     * Met à jour la base de données à partir des csv stockés en local
     * @param context Le contexte de l'application
     * @throws Exception Renvoyée si le CSV est invalide, où si une erreur SQL apparaît
     */
    public void updateLocal(Context context) throws Exception {
        CSVReader contactReader, entrepriseReader, stageReader, stagiaireReader;

        contactReader = new CSVReader(new InputStreamReader(context.getResources().openRawResource(R.raw.contact)), ',', '"', 1);
        entrepriseReader = new CSVReader(new InputStreamReader(context.getResources().openRawResource(R.raw.entreprise)), ',', '"', 1);
        stageReader = new CSVReader(new InputStreamReader(context.getResources().openRawResource(R.raw.stage)), ',', '"', 1);
        stagiaireReader = new CSVReader(new InputStreamReader(context.getResources().openRawResource(R.raw.stagiaire)), ',', '"', 1);

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            this.reinit(db);
            this.onCreate(db);
            // On initialise les entreprises
            this.readEntreprise(entrepriseReader, db, false, context);
            this.readStagiaire(stagiaireReader, db);
            this.readStage(stageReader, db);
            this.readContact(contactReader, db);
            db.setTransactionSuccessful();
        }catch(SQLiteException sqe) {
            throw new InvalidCSVException(InvalidCSVException.Cause.REFERENCE_INVALIDE, sqe.getLocalizedMessage());
        }catch (IOException ioe){
            throw new InvalidCSVException(InvalidCSVException.Cause.VALEUR_INVALIDE, "");
        }finally {
            db.endTransaction();
            db.close();
        }
    }

    /**
     * Ajoute une valeur null si la chaine de caractères est vide
     * @param cv L'objet ContentValues qui contient les valeurs
     * @param champ La chaine de caractères à ne pas ajouter vide dans le COntentValues
     * @param valeur
     */
    private void putIfNull(ContentValues cv, String champ, String valeur) {
        if(valeur.isEmpty())
            cv.putNull(champ);
        else
            cv.put(champ, valeur);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Entreprise(" +
                "nom_entreprise TEXT," +
                "site_web TEXT," +
                "abbr TEXT PRIMARY KEY)");

        db.execSQL("CREATE TABLE Localisation(" +
                "nom TEXT," +
                "latitude REAL NULL," +
                "longitude REAL NULL," +
                "adresse TEXT NULL," +
                "entreprise TEXT," +
                "FOREIGN KEY (entreprise) REFERENCES Entreprise(abbr))");

        db.execSQL("CREATE TABLE Contact(" +
                "civilite INTEGER," +
                "nom TEXT," +
                "prenom TEXT NULL," +
                "entreprise TEXT," +
                "telephone TEXT NULL," +
                "mail TEXT NULL," +
                "poste TEXT," +
                "FOREIGN KEY(entreprise) REFERENCES Entreprise(abbr))");

        db.execSQL("CREATE TABLE Stagiaire(" +
                "nom TEXT," +
                "prenom TEXT," +
                "login TEXT PRIMARY KEY," +
                "promotion TEXT," +
                "mail TEXT NULL," +
                "tel TEXT NULL)");

        db.execSQL("CREATE TABLE Emploi(" +
                "entreprise TEXT," +
                "stagiaire TEXT," +
                "poste TEXT," +
                "FOREIGN KEY(entreprise) REFERENCES Entreprise(abbr)," +
                "FOREIGN KEY(stagiaire) REFERENCES Stagiaire(login))");

        db.execSQL("CREATE TABLE Stage(" +
                "sujet TEXT," +
                "mots_cles TEXT NULL," +
                "lien_rapport TEXT NULL," +
                "stagiaire TEXT," +
                "entreprise TEXT," +
                "date_debut TEXT," +
                "date_fin TEXT," +
                "nom_maitre_stage TEXT NULL," +
                "nom_tuteur_stage TEXT NULL," +
                "FOREIGN KEY(stagiaire) REFERENCES Stagiaire(login)," +
                "FOREIGN KEY(entreprise) REFERENCES Entreprise(abbr))");

    }

    /**
     * Méthode appelée pour supprimer le contenu de la base de données
     * @param db La base de données à supprimer
     */
    private void reinit(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS Stage");
        db.execSQL("DROP TABLE IF EXISTS Emploi");
        db.execSQL("DROP TABLE IF EXISTS Stagiaire");
        db.execSQL("DROP TABLE IF EXISTS Contact");
        db.execSQL("DROP TABLE IF EXISTS Localisation");
        db.execSQL("DROP TABLE IF EXISTS Entreprise");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        reinit(db);
        onCreate(db);
    }

}
