package br.gov.sp.cps.trocabook.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.*;
import android.util.Patterns;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.service.UsuarioService;

public class DadosSegurancaActivity extends AppCompatActivity {

    private TextInputEditText editTelefone, editEmailRec, editNascimento, editCPF, editRG;
    private TextInputLayout layoutEmailRec, layoutCPF, layoutTelefone, layoutNascimento;
    private RadioGroup radioGenero;
    private String emailPrincipal;

    private FirebaseAuth mAuth;
    private UsuarioService usuarioService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dados_seguranca);

        mAuth = FirebaseAuth.getInstance();
        usuarioService = new UsuarioService();

        emailPrincipal = getIntent().getStringExtra("EMAIL_USUARIO");

        editTelefone = findViewById(R.id.editTelefone);
        editEmailRec = findViewById(R.id.editEmailRec);
        editNascimento = findViewById(R.id.editNascimento);
        editCPF = findViewById(R.id.editCPF);
        editRG = findViewById(R.id.editRG);
        radioGenero = findViewById(R.id.radioGenero);

        layoutEmailRec = findViewById(R.id.layoutEmailRec);
        layoutCPF = findViewById(R.id.layoutCPF);
        layoutTelefone = findViewById(R.id.layoutTelefone);
        layoutNascimento = findViewById(R.id.layoutNascimento);

        configurarMascaras();
        configurarDataCalendario();
        configurarValidacoesAoSair();

        findViewById(R.id.btnVoltarSeguranca).setOnClickListener(v -> finish());

        findViewById(R.id.btnEnviarVerificacao).setOnClickListener(v -> {
            if (validarTudoAntesDeEnviar()) {
                salvarDados();
            }
        });
    }


    private void salvarDados() {

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        String emailRec = editEmailRec.getText().toString().trim();
        String telefone = editTelefone.getText().toString().trim();
        String cpf = editCPF.getText().toString().replaceAll("[^\\d]", "");
        String rg = editRG.getText().toString().trim();
        String nascimento = editNascimento.getText().toString().trim();

        int idGenero = radioGenero.getCheckedRadioButtonId();
        RadioButton rb = findViewById(idGenero);
        String genero = rb.getText().toString();

        usuarioService.salvarDadosUsuario(
                userId,
                emailPrincipal,
                emailRec,
                telefone,
                cpf,
                rg,
                nascimento,
                genero,
                new UsuarioService.ServiceCallback<Void>() {

                    @Override
                    public void onSuccess(Void resultado) {
                        Intent intent = new Intent(DadosSegurancaActivity.this, EscolhaVerificacaoActivity.class);
                        intent.putExtra("email_recuperacao", emailRec);
                        intent.putExtra("telefone", telefone);
                        intent.putExtra("EMAIL_USUARIO", emailPrincipal);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(String erro) {
                        Toast.makeText(DadosSegurancaActivity.this, erro, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }


    private void configurarValidacoesAoSair() {

        editEmailRec.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String emailRec = editEmailRec.getText().toString().trim().toLowerCase();

                if (emailRec.length() > 0) {

                    if (!Patterns.EMAIL_ADDRESS.matcher(emailRec).matches() || !emailRec.endsWith("@gmail.com")) {
                        layoutEmailRec.setError("Use um email @gmail.com válido");

                    } else if (!usuarioService.validarEmailRecuperacao(emailPrincipal, emailRec)) {
                        layoutEmailRec.setError("O email deve ser diferente do principal");

                    } else {
                        layoutEmailRec.setError(null);
                    }
                }
            }
        });

        editTelefone.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String tel = editTelefone.getText().toString().replaceAll("[^\\d]", "");

                if (tel.length() > 0 && tel.length() < 10) {
                    layoutTelefone.setError("Telefone incompleto");
                } else {
                    layoutTelefone.setError(null);
                }
            }
        });

        editCPF.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String cpf = editCPF.getText().toString().replaceAll("[^\\d]", "");

                if (cpf.length() > 0 && !usuarioService.isCpfValido(cpf)) {
                    layoutCPF.setError("CPF inválido");
                } else {
                    layoutCPF.setError(null);
                }
            }
        });
    }

    private boolean validarTudoAntesDeEnviar() {

        boolean erro = false;

        String emailRec = editEmailRec.getText().toString().trim().toLowerCase();
        String cpf = editCPF.getText().toString().replaceAll("[^\\d]", "");

        if (!usuarioService.validarEmailRecuperacao(emailPrincipal, emailRec)) {
            layoutEmailRec.setError("Email inválido");
            erro = true;
        }

        if (!usuarioService.isCpfValido(cpf)) {
            layoutCPF.setError("CPF inválido");
            erro = true;
        }

        if (editNascimento.getText().toString().isEmpty()) {
            layoutNascimento.setError("Informe sua data");
            erro = true;
        }

        if (radioGenero.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Selecione o gênero", Toast.LENGTH_SHORT).show();
            erro = true;
        }

        return !erro;
    }

    private void configurarDataCalendario() {
        editNascimento.setOnClickListener(v -> {

            Calendar c = Calendar.getInstance();

            DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, day) -> {

                if (usuarioService.isMaiorDeIdade(year, month, day)) {

                    editNascimento.setText(String.format("%02d/%02d/%d", day, month + 1, year));
                    layoutNascimento.setError(null);

                } else {
                    editNascimento.setText("");
                    layoutNascimento.setError("Apenas maiores de 18 anos");
                    Toast.makeText(this, "Cadastro negado", Toast.LENGTH_LONG).show();
                }

            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

            datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePicker.show();
        });
    }
    private void configurarMascaras() {
        editTelefone.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString().replaceAll("[^\\d]", "");
                if (isUpdating) { isUpdating = false; return; }
                isUpdating = true;
                String mask = (str.length() > 10) ? "(##) #####-####" : "(##) ####-####";
                StringBuilder formatado = new StringBuilder();
                int i = 0;
                for (char m : mask.toCharArray()) {
                    if (m != '#') { formatado.append(m); continue; }
                    try { formatado.append(str.charAt(i)); } catch (Exception e) { break; }
                    i++;
                }
                editTelefone.setText(formatado.toString());
                editTelefone.setSelection(formatado.length());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        editCPF.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString().replaceAll("[^\\d]", "");
                if (isUpdating) { isUpdating = false; return; }
                isUpdating = true;
                String mask = "";
                if (str.length() > 0) {
                    if (str.length() <= 3) mask = str;
                    else if (str.length() <= 6) mask = str.substring(0,3) + "." + str.substring(3);
                    else if (str.length() <= 9) mask = str.substring(0,3) + "." + str.substring(3,6) + "." + str.substring(6);
                    else mask = str.substring(0,3) + "." + str.substring(3,6) + "." + str.substring(6,9) + "-" + str.substring(9);
                }
                editCPF.setText(mask);
                editCPF.setSelection(mask.length());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        editRG.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString().replaceAll("[^\\d]", "");
                if (isUpdating) { isUpdating = false; return; }
                isUpdating = true;
                String mask = str;
                if (str.length() > 8) mask = str.substring(0,2) + "." + str.substring(2,5) + "." + str.substring(5,8) + "-" + str.substring(8);
                else if (str.length() > 5) mask = str.substring(0,2) + "." + str.substring(2,5) + "." + str.substring(5);
                else if (str.length() > 2) mask = str.substring(0,2) + "." + str.substring(2);
                editRG.setText(mask);
                editRG.setSelection(mask.length());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }
}