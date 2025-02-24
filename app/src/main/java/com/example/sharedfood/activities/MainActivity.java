    package com.example.sharedfood.activities;

    import android.content.Intent;
    import android.os.Bundle;
    import android.util.Log;
    import android.widget.Button;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;

    import com.example.sharedfood.R;
    import com.example.sharedfood.activitiesAuthentication.LoginActivity;
    import com.example.sharedfood.activitiesAuthentication.SignUpActivity;
    import com.facebook.AccessToken;
    import com.facebook.CallbackManager;
    import com.facebook.FacebookCallback;
    import com.facebook.FacebookException;
    import com.facebook.login.LoginResult;
    import com.facebook.login.widget.LoginButton;
    import com.google.android.gms.auth.api.signin.GoogleSignIn;
    import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
    import com.google.android.gms.auth.api.signin.GoogleSignInClient;
    import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
    import com.google.android.gms.common.api.ApiException;
    import com.google.android.gms.tasks.OnCompleteListener;
    import com.google.android.gms.tasks.Task;
    import com.google.firebase.auth.AuthCredential;
    import com.google.firebase.auth.AuthResult;
    import com.google.firebase.auth.FacebookAuthProvider;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.android.gms.common.SignInButton;
    import com.facebook.FacebookSdk;
    import com.google.firebase.auth.GoogleAuthProvider;
    import com.google.firebase.firestore.FirebaseFirestore;

    public class MainActivity extends AppCompatActivity {

        // UI elements for login and sign-up
        private Button loginButton, signUpButton;
        private SignInButton googleSignInButton;

        // Firebase authentication and Google sign-in client
        private FirebaseAuth firebaseAuth;
        private GoogleSignInClient googleSignInClient;

        // Callback manager for Facebook authentication
        CallbackManager mCallbackManager;

        // Firebase user instance
        FirebaseUser user;

        // Request code for Google sign-in
        private static final int RC_SIGN_IN = 100;

        // Tag for logging
        private static final String TAG = "MainActivity";

        // Michael - Check if the user is an admin - START, 26/01/2025 - SSSSSSSSSSSSSSSSSSSSSSSSSSS
        /**
         * Checks if the given user is an admin by querying the Firestore "admins" collection.
         * Calls the listener with true if the user is an admin, otherwise false.
         *
         * @param user     The FirebaseUser instance representing the logged-in user.
         * @param listener Callback interface to handle the result asynchronously.
         */
        public static void isAdmin(FirebaseUser user, OnAdminCheckCompleteListener listener) {
            if (user == null || user.getEmail() == null) {
                listener.onComplete(false); // User is not logged in or email is null
                return;
            }

            FirebaseFirestore.getInstance()
                    .collection("admins") // Collection storing admin user records
                    .document(user.getEmail().trim()) // Check if the email exists as a document ID
                    .get()
                    .addOnSuccessListener(documentSnapshot ->
                            listener.onComplete(documentSnapshot.exists()) // If document exists, user is an admin
                    )
                    .addOnFailureListener(e -> {
                        Log.e("MainActivity", "Error checking admin status", e);
                        listener.onComplete(false); // Error occurred, assume user is not an admin
                    });
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            // Initialize UI components (buttons for login, sign-up, and Google sign-in)
            loginButton = findViewById(R.id.loginButton);
            signUpButton = findViewById(R.id.signUpButton);
            googleSignInButton = findViewById(R.id.googleSignInButton);

            // Initialize Firebase Authentication instance
            firebaseAuth = FirebaseAuth.getInstance();

            // Configure Google Sign-In options
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id)) // Fetch the Client ID from strings.xml
                    .requestEmail() // Request user's email
                    .build();

            // Initialize Google Sign-In client
            googleSignInClient = GoogleSignIn.getClient(this, gso);

            // Check if a user is already signed in
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                // If user is already signed in, navigate to HomePageActivity
                if (currentUser.getEmail().trim().equalsIgnoreCase("mici9578@gmail.com")) {
                    // Special case for a specific email (maybe admin or test account)
                    Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // General case for all other signed-in users
                    Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
                    startActivity(intent);
                    finish(); // Finish this activity to prevent going back to login screen
                    return;
                }
            }

            // Set click listener for login button -> Opens LoginActivity
            loginButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            });

            // Set click listener for sign-up button -> Opens SignUpActivity
            signUpButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            });

            // Set click listener for Google Sign-In button
            googleSignInButton.setOnClickListener(v -> signInWithGoogle());

            // Initialize Facebook SDK (required for Facebook login)
            FacebookSdk.sdkInitialize(MainActivity.this);

            // Initialize Facebook Login button
            mCallbackManager = CallbackManager.Factory.create();
            LoginButton loginButton = findViewById(R.id.facebook_login_button);
            loginButton.setReadPermissions("email", "public_profile"); // Request email and profile permissions

            // Register callback for Facebook login
            loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.d(TAG, "facebook:onSuccess:" + loginResult);
                    handleFacebookAccessToken(loginResult.getAccessToken()); // Handle successful login
                }

                @Override
                public void onCancel() {
                    Log.d(TAG, "facebook:onCancel"); // User canceled login
                }

                @Override
                public void onError(FacebookException error) {
                    Log.d(TAG, "facebook:onError", error); // Error occurred during login
                }
            });
        }

        // Interface for asynchronous admin check (callback for admin verification)
        public interface OnAdminCheckCompleteListener {
            void onComplete(boolean isAdmin); // Called when the admin status check is complete
        }
        // Michael, 26/01/2025 - END: ממשק לבדיקה אסינכרונית של משתמשים מנהלים

        // Start the Google Sign-In process
        private void signInWithGoogle() {
            Intent signInIntent = googleSignInClient.getSignInIntent(); // Get the Google sign-in intent
            startActivityForResult(signInIntent, RC_SIGN_IN); // Launch the sign-in activity
        }

        // Handle the result of the Google Sign-In process
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            // Pass the result to the Facebook SDK (if login was via Facebook)
            mCallbackManager.onActivityResult(requestCode, resultCode, data);

            if (requestCode == RC_SIGN_IN) { // Check if the request is for Google Sign-In
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    // Retrieve the signed-in Google account
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null) {
                        // If sign-in was successful, authenticate the user with Firebase
                        firebaseAuthWithGoogle(account);
                    }
                } catch (ApiException e) {
                    // Handle sign-in failure (e.g., user canceled or error occurred)
                    Log.w(TAG, "Google sign in failed", e);
                    Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        }

        // Authenticate the Google account with Firebase Authentication
        private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
            // Get authentication credentials from the Google account
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

            // Sign in to Firebase using the Google credentials
            firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // If authentication is successful, get the current user
                            user = firebaseAuth.getCurrentUser();

                            // Show a welcome message with the user's name
                            Toast.makeText(MainActivity.this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();

                            // Navigate to HomePageActivity after successful sign-in
                            Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
                            startActivity(intent);
                        } else {
                            // If authentication failed, show an error message
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        /**
         * Handles Facebook login by exchanging the access token for Firebase authentication credentials.
         * @param token The Facebook access token obtained after successful login.
         */
        private void handleFacebookAccessToken(AccessToken token) {
            Log.d(TAG, "handleFacebookAccessToken:" + token); // Log the received token for debugging.

            // Create Firebase authentication credentials from the Facebook access token
            AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

            // Authenticate the user with Firebase using the obtained credentials
            firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // If sign-in is successful, retrieve the authenticated Firebase user
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                updateUI(user); // Update the UI to reflect the successful login
                            } else {
                                // If sign-in fails, show an error message to the user
                                Toast.makeText(MainActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                updateUI(null); // Reset UI to default (not logged in)
                            }
                        }
                    });
        }

        /**
         * Updates the UI based on the user's authentication status.
         * If the user is signed in, navigate to HomePageActivity.
         * If not, show a message prompting the user to sign in.
         *
         * @param user The authenticated Firebase user (null if login failed).
         */
        private void updateUI(FirebaseUser user) {
            if(user != null) {
                // If the user is authenticated, navigate to HomePageActivity
                Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
                startActivity(intent);
                finish(); // Close MainActivity to prevent going back to login screen
            } else {
                // If authentication failed, prompt the user to sign in again
                Toast.makeText(this, "Please sign in to continue", Toast.LENGTH_SHORT).show();
            }
        }
    }
