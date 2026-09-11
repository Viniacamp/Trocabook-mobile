package br.gov.sp.cps.trocabook.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.Locale;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.Usuario;
import br.gov.sp.cps.trocabook.service.UsuarioService;

public class DadosSegurancaActivity extends AppCompatActivity {

    private TextInputEditText editTelefone;
    private TextInputEditText editEmailRec;
    private TextInputEditText editNascimento;
    private TextInputEditText editCPF;
    private TextInputEditText editRG;

    private TextInputLayout layoutEmailRec;
    private TextInputLayout layoutCPF;
    private TextInputLayout layoutTelefone;
    private TextInputLayout layoutNascimento;

    private RadioGroup radioGenero;

    private String nomeUsuario;
    private String emailPrincipal;

    private FirebaseAuth mAuth;
    private UsuarioService usuarioService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dados_seguranca);

        mAuth = FirebaseAuth.getInstance();
        usuarioService = new UsuarioService();

        nomeUsuario = getIntent().getStringExtra("NOME_USUARIO");
        emailPrincipal = getIntent().getStringExtra("EMAIL_USUARIO");

        inicializarComponentes();

        configurarMascaras();
        configurarDataCalendario();
        configurarValidacoesAoSair();

        findViewById(R.id.btnVoltarSeguranca)
                .setOnClickListener(v -> finish());

        findViewById(R.id.btnEnviarVerificacao)
                .setOnClickListener(v -> {

                    if (validarTudoAntesDeEnviar()) {
                        salvarDados();
                    }
                });
    }

    private void inicializarComponentes() {

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
    }

    private void salvarDados() {

        if (mAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Usuário não autenticado!",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        String emailRec = editEmailRec
                .getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        String telefone = editTelefone
                .getText()
                .toString()
                .trim();

        String cpf = editCPF
                .getText()
                .toString()
                .replaceAll("[^\\d]", "");

        String rg = editRG
                .getText()
                .toString()
                .trim();

        String nascimento = editNascimento
                .getText()
                .toString()
                .trim();

        int idGenero = radioGenero.getCheckedRadioButtonId();

        if (idGenero == -1) {
            Toast.makeText(
                    this,
                    "Selecione o gênero",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        RadioButton rbGenero = findViewById(idGenero);
        String genero = rbGenero.getText().toString();

        usuarioService.salvarDadosUsuario(
                userId,
                nomeUsuario,
                emailPrincipal,
                emailRec,
                telefone,
                cpf,
                rg,
                nascimento,
                genero,
                "",
                new UsuarioService.ServiceCallback<Usuario>() {

                    @Override
                    public void onSucesso(Usuario usuario) {

                        Intent intent = new Intent(
                                DadosSegurancaActivity.this,
                                EscolhaVerificacaoActivity.class
                        );

                        intent.putExtra(
                                "email_recuperacao",
                                emailRec
                        );

                        intent.putExtra(
                                "telefone",
                                telefone
                        );

                        intent.putExtra(
                                "EMAIL_USUARIO",
                                emailPrincipal
                        );

                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onErro(String erro) {

                        Toast.makeText(
                                DadosSegurancaActivity.this,
                                erro,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void configurarValidacoesAoSair() {

        editEmailRec.setOnFocusChangeListener((v, hasFocus) -> {

            if (!hasFocus) {

                String emailRec = editEmailRec
                        .getText()
                        .toString()
                        .trim()
                        .toLowerCase(Locale.ROOT);

                if (!emailRec.isEmpty()) {

                    if (!Patterns.EMAIL_ADDRESS
                            .matcher(emailRec)
                            .matches()
                            || !emailRec.endsWith("@gmail.com")) {

                        layoutEmailRec.setError(
                                "Use um email @gmail.com válido"
                        );

                    } else if (!usuarioService
                            .validarEmailRecuperacao(
                                    emailPrincipal,
                                    emailRec
                            )) {

                        layoutEmailRec.setError(
                                "O email deve ser diferente do principal"
                        );

                    } else {

                        layoutEmailRec.setError(null);
                    }
                }
            }
        });

        editTelefone.setOnFocusChangeListener((v, hasFocus) -> {

            if (!hasFocus) {

                String telefone = editTelefone
                        .getText()
                        .toString()
                        .replaceAll("[^\\d]", "");

                if (telefone.length() > 0
                        && telefone.length() < 10) {

                    layoutTelefone.setError(
                            "Telefone incompleto"
                    );

                } else {

                    layoutTelefone.setError(null);
                }
            }
        });

        editCPF.setOnFocusChangeListener((v, hasFocus) -> {

            if (!hasFocus) {

                String cpf = editCPF
                        .getText()
                        .toString()
                        .replaceAll("[^\\d]", "");

                if (!cpf.isEmpty()
                        && !usuarioService.isCpfValido(cpf)) {

                    layoutCPF.setError("CPF inválido");

                } else {

                    layoutCPF.setError(null);
                }
            }
        });
    }

    private boolean validarTudoAntesDeEnviar() {

        boolean erro = false;

        String emailRec = editEmailRec
                .getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        String cpf = editCPF
                .getText()
                .toString()
                .replaceAll("[^\\d]", "");

        String telefone = editTelefone
                .getText()
                .toString()
                .replaceAll("[^\\d]", "");

        if (nomeUsuario == null
                || nomeUsuario.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Nome do usuário não encontrado",
                    Toast.LENGTH_SHORT
            ).show();

            erro = true;
        }

        if (emailPrincipal == null
                || emailPrincipal.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Email principal não encontrado",
                    Toast.LENGTH_SHORT
            ).show();

            erro = true;
        }

        if (!usuarioService.validarEmailRecuperacao(
                emailPrincipal,
                emailRec
        )) {

            layoutEmailRec.setError(
                    "Email de recuperação inválido"
            );

            erro = true;

        } else {

            layoutEmailRec.setError(null);
        }

        if (!usuarioService.isCpfValido(cpf)) {

            layoutCPF.setError("CPF inválido");
            erro = true;

        } else {

            layoutCPF.setError(null);
        }

        if (telefone.length() < 10) {

            layoutTelefone.setError(
                    "Telefone incompleto"
            );

            erro = true;

        } else {

            layoutTelefone.setError(null);
        }

        if (editNascimento
                .getText()
                .toString()
                .trim()
                .isEmpty()) {

            layoutNascimento.setError(
                    "Informe sua data"
            );

            erro = true;

        } else {

            layoutNascimento.setError(null);
        }

        if (radioGenero.getCheckedRadioButtonId() == -1) {

            Toast.makeText(
                    this,
                    "Selecione o gênero",
                    Toast.LENGTH_SHORT
            ).show();

            erro = true;
        }

        return !erro;
    }

    private void configurarDataCalendario() {

        editNascimento.setOnClickListener(v -> {

            Calendar c = Calendar.getInstance();

            DatePickerDialog datePicker =
                    new DatePickerDialog(
                            this,
                            (view, year, month, day) -> {

                                if (usuarioService.isMaiorDeIdade(
                                        year,
                                        month,
                                        day
                                )) {

                                    editNascimento.setText(
                                            String.format(
                                                    Locale.ROOT,
                                                    "%02d/%02d/%d",
                                                    day,
                                                    month + 1,
                                                    year
                                            )
                                    );

                                    layoutNascimento.setError(null);

                                } else {

                                    editNascimento.setText("");

                                    layoutNascimento.setError(
                                            "Apenas maiores de 18 anos"
                                    );

                                    Toast.makeText(
                                            this,
                                            "Cadastro negado",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            },
                            c.get(Calendar.YEAR),
                            c.get(Calendar.MONTH),
                            c.get(Calendar.DAY_OF_MONTH)
                    );

            datePicker.getDatePicker()
                    .setMaxDate(System.currentTimeMillis());

            datePicker.show();
        });
    }

    private void configurarMascaras() {

        editTelefone.addTextChangedListener(
                new TextWatcher() {

                    private boolean isUpdating = false;

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        if (isUpdating) {
                            isUpdating = false;
                            return;
                        }

                        String str = s
                                .toString()
                                .replaceAll("[^\\d]", "");

                        isUpdating = true;

                        String mask = str.length() > 10
                                ? "(##) #####-####"
                                : "(##) ####-####";

                        StringBuilder formatado =
                                new StringBuilder();

                        int i = 0;

                        for (char m : mask.toCharArray()) {

                            if (m != '#') {

                                formatado.append(m);
                                continue;
                            }

                            if (i >= str.length()) {
                                break;
                            }

                            formatado.append(str.charAt(i));
                            i++;
                        }

                        editTelefone.setText(
                                formatado.toString()
                        );

                        editTelefone.setSelection(
                                formatado.length()
                        );
                    }

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );

        editCPF.addTextChangedListener(
                new TextWatcher() {

                    private boolean isUpdating = false;

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        if (isUpdating) {
                            isUpdating = false;
                            return;
                        }

                        String str = s
                                .toString()
                                .replaceAll("[^\\d]", "");

                        isUpdating = true;

                        String mask;

                        if (str.isEmpty()) {

                            mask = "";

                        } else if (str.length() <= 3) {

                            mask = str;

                        } else if (str.length() <= 6) {

                            mask = str.substring(0, 3)
                                    + "."
                                    + str.substring(3);

                        } else if (str.length() <= 9) {

                            mask = str.substring(0, 3)
                                    + "."
                                    + str.substring(3, 6)
                                    + "."
                                    + str.substring(6);

                        } else {

                            mask = str.substring(0, 3)
                                    + "."
                                    + str.substring(3, 6)
                                    + "."
                                    + str.substring(6, 9)
                                    + "-"
                                    + str.substring(9);
                        }

                        editCPF.setText(mask);

                        editCPF.setSelection(
                                mask.length()
                        );
                    }

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );

        editRG.addTextChangedListener(
                new TextWatcher() {

                    private boolean isUpdating = false;

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        if (isUpdating) {
                            isUpdating = false;
                            return;
                        }

                        String str = s
                                .toString()
                                .replaceAll("[^\\d]", "");

                        isUpdating = true;

                        String mask = str;

                        if (str.length() > 8) {

                            mask = str.substring(0, 2)
                                    + "."
                                    + str.substring(2, 5)
                                    + "."
                                    + str.substring(5, 8)
                                    + "-"
                                    + str.substring(8);

                        } else if (str.length() > 5) {

                            mask = str.substring(0, 2)
                                    + "."
                                    + str.substring(2, 5)
                                    + "."
                                    + str.substring(5);

                        } else if (str.length() > 2) {

                            mask = str.substring(0, 2)
                                    + "."
                                    + str.substring(2);
                        }

                        editRG.setText(mask);

                        editRG.setSelection(
                                mask.length()
                        );
                    }

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );
    }
}