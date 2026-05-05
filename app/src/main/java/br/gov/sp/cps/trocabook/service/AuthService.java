package br.gov.sp.cps.trocabook.service;

import android.app.Activity;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import br.gov.sp.cps.trocabook.BuildConfig;

public class AuthService {

    private static final String TAG = "TROCABOOK_AUTH";

    private final FirebaseAuth mAuth;
    private final CredentialManager credentialManager;
    private final Executor executor;

    public AuthService(Activity activity) {
        this.mAuth = FirebaseAuth.getInstance();
        this.credentialManager = CredentialManager.create(activity);
        this.executor = Executors.newSingleThreadExecutor();
    }


    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String erro);
    }

    public interface PhoneCallback {
        void onCodeSent(String verificationId);
        void onError(String erro);
    }

    public FirebaseUser getUsuarioAtual() {
        return mAuth.getCurrentUser();
    }


    public void autenticarComGoogle(Activity activity, AuthCallback callback) {

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                activity,
                request,
                null,
                executor,
                new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {

                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignIn(result, activity, callback);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.e(TAG, "Erro CredentialManager", e);
                        activity.runOnUiThread(() ->
                                callback.onError("Erro ao autenticar com Google")
                        );
                    }
                }
        );
    }

    private void handleSignIn(GetCredentialResponse result, Activity activity, AuthCallback callback) {

        androidx.credentials.Credential credential = result.getCredential();

        try {
            GoogleIdTokenCredential googleIdToken;

            if (credential instanceof GoogleIdTokenCredential) {
                googleIdToken = (GoogleIdTokenCredential) credential;
            } else {
                googleIdToken = GoogleIdTokenCredential.createFrom(credential.getData());
            }

            autenticarNoFirebase(googleIdToken.getIdToken(), activity, callback);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar credencial", e);
            activity.runOnUiThread(() ->
                    callback.onError("Erro ao processar login Google")
            );
        }
    }

    private void autenticarNoFirebase(String idToken, Activity activity, AuthCallback callback) {

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {

                    if (task.isSuccessful()) {
                        FirebaseUser user = this.getUsuarioAtual();

                        activity.runOnUiThread(() ->
                                callback.onSuccess(user)
                        );

                    } else {
                        Log.e(TAG, "Erro Firebase Auth", task.getException());

                        activity.runOnUiThread(() ->
                                callback.onError("Erro ao autenticar no Firebase")
                        );
                    }
                });
    }

    public void cadastrarComEmail(String email, String senha, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(this.getUsuarioAtual());
                    } else {
                        String erro;
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthUserCollisionException e) {
                            erro = "Este e-mail já está cadastrado.";
                        } catch (Exception e) {
                            erro = "Erro ao cadastrar: " + e.getMessage();
                        }
                        callback.onError(erro);
                    }
                });
    }

    public void enviarVerificacaoEmail(AuthCallback callback) {
        FirebaseUser user = this.getUsuarioAtual();

        if (user == null) {
            callback.onError("Usuário não autenticado");
            return;
        }

        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(mAuth.getCurrentUser());
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    public void enviarSMS(String telefone, AppCompatActivity activity, PhoneCallback callback) {

        if (telefone == null || telefone.isEmpty()) {
            callback.onError("Telefone inválido");
            return;
        }

        String telLimpo = telefone.replaceAll("[^\\d]", "");
        String telFinal = "+55" + telLimpo;

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(telFinal)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onCodeSent(String verificationId,
                                           PhoneAuthProvider.ForceResendingToken token) {

                        callback.onCodeSent(verificationId);
                    }

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {

                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        callback.onError(e.getMessage());
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void loginComEmail(String email, String senha, AuthCallback callback) {

        if (email.isEmpty() || senha.isEmpty()) {
            callback.onError("Preencha todos os campos");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(mAuth.getCurrentUser());
                    } else {
                        callback.onError("E-mail ou senha incorretos");
                    }
                });
    }

    public void enviarResetSenha(String email, AuthCallback callback) {

        if (email == null || email.isEmpty()) {
            callback.onError("E-mail inválido");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError("E-mail não cadastrado ou erro ao enviar");
                    }
                });
    }

    public void verificarEmailVerificado(AuthCallback callback) {

        FirebaseUser user = this.getUsuarioAtual();

        if (user == null) {
            callback.onError("Usuário não autenticado");
            return;
        }

        user.reload().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                if (user.isEmailVerified()) {
                    callback.onSuccess(user);
                } else {
                    callback.onError("Email ainda não verificado");
                }

            } else {
                callback.onError("Erro ao verificar email");
            }
        });
    }

    public void confirmarCodigoSMS(String verificationId, String codigo, AuthCallback callback) {

        if (verificationId == null || codigo == null || codigo.length() < 6) {
            callback.onError("Código inválido");
            return;
        }

        FirebaseUser user = this.getUsuarioAtual();

        if (user == null) {
            callback.onError("Usuário não autenticado");
            return;
        }

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(verificationId, codigo);

        user.updatePhoneNumber(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(user);
                    } else {
                        callback.onError("Código inválido");
                    }
                });
    }
}