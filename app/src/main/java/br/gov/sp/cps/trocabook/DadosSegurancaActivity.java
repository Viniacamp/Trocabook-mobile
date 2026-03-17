package br.gov.sp.cps.trocabook;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Calendar;

public class DadosSegurancaActivity extends AppCompatActivity {

    private TextInputEditText editTelefone, editEmailRec, editNascimento, editCPF, editRG;
    private TextInputLayout layoutEmailRec, layoutCPF, layoutTelefone, layoutNascimento;
    private RadioGroup radioGenero;
    private String emailPrincipal; // Variável para armazenar o email do cadastro

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dados_seguranca);

        // Recupera o email vindo do Google Login ou Cadastro Manual
        emailPrincipal = getIntent().getStringExtra("EMAIL_USUARIO");

        // Inicialização dos componentes
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

        // Configurações iniciais
        configurarMascaras();
        configurarDataCalendario();
        configurarValidacoesAoSair();

        // Botão Voltar
        findViewById(R.id.btnVoltarSeguranca).setOnClickListener(v -> finish());

        // Botão Enviar
        findViewById(R.id.btnEnviarVerificacao).setOnClickListener(v -> {
            if (validarTudoAntesDeEnviar()) {
                // Navega para a próxima tela (Verificação)
                Intent intent = new Intent(this, VerificacaoActivity.class);
                intent.putExtra("email_recuperacao", editEmailRec.getText().toString());
                intent.putExtra("telefone", editTelefone.getText().toString());
                startActivity(intent);
            }
        });
    }

    private void configurarValidacoesAoSair() {
        // Validação do Email de Recuperação
        editEmailRec.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String emailRec = editEmailRec.getText().toString().trim().toLowerCase();
                if (emailRec.length() > 0) {
                    if (!Patterns.EMAIL_ADDRESS.matcher(emailRec).matches() || !emailRec.endsWith("@gmail.com")) {
                        layoutEmailRec.setError("Use um email @gmail.com válido");
                    } else if (emailPrincipal != null && emailRec.equals(emailPrincipal.toLowerCase())) {
                        layoutEmailRec.setError("O email de recuperação deve ser diferente do principal");
                    } else {
                        layoutEmailRec.setError(null);
                    }
                }
            }
        });

        // Validação do Telefone
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

        // Validação do CPF
        editCPF.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String cpf = editCPF.getText().toString().replaceAll("[^\\d]", "");
                if (cpf.length() > 0 && !isCpfValido(cpf)) {
                    layoutCPF.setError("CPF inválido ou inexistente");
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

        // Validação Email de Recuperação (Formato e Comparação)
        if (!emailRec.endsWith("@gmail.com")) {
            layoutEmailRec.setError("Use um email @gmail.com");
            erro = true;
        } else if (emailPrincipal != null && emailRec.equals(emailPrincipal.toLowerCase())) {
            layoutEmailRec.setError("O email de recuperação não pode ser igual ao principal");
            erro = true;
        }

        // Validação CPF
        if (!isCpfValido(cpf)) {
            layoutCPF.setError("CPF inválido");
            erro = true;
        }

        // Validação Nascimento (Maioridade)
        if (editNascimento.getText().toString().isEmpty()) {
            layoutNascimento.setError("É necessário ter mais de 18 anos");
            erro = true;
        }

        // Validação Gênero
        if (radioGenero.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Selecione o gênero", Toast.LENGTH_SHORT).show();
            erro = true;
        }

        return !erro;
    }

    private boolean isCpfValido(String cpf) {
        if (cpf == null || cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) return false;
        try {
            int d1 = 0, d2 = 0, peso = 10;
            for (int i = 0; i < 9; i++) {
                int num = Integer.parseInt(cpf.substring(i, i + 1));
                d1 += num * peso;
                d2 += num * (peso + 1);
                peso--;
            }
            int r1 = 11 - (d1 % 11);
            int digito1 = (r1 > 9) ? 0 : r1;
            d2 += digito1 * 2;
            int r2 = 11 - (d2 % 11);
            int digito2 = (r2 > 9) ? 0 : r2;
            return cpf.substring(9).equals("" + digito1 + digito2);
        } catch (Exception e) { return false; }
    }

    private void configurarDataCalendario() {
        editNascimento.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, day) -> {

                if (isMaiorDeIdade(year, month, day)) {
                    editNascimento.setText(String.format("%02d/%02d/%d", day, month + 1, year));
                    layoutNascimento.setError(null);
                } else {
                    editNascimento.setText("");
                    layoutNascimento.setError("O Trocabook é apenas para maiores de 18 anos.");
                    Toast.makeText(this, "Cadastro negado para menores", Toast.LENGTH_LONG).show();
                }

            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

            datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePicker.show();
        });
    }

    private boolean isMaiorDeIdade(int year, int month, int day) {
        Calendar dataNascimento = Calendar.getInstance();
        dataNascimento.set(year, month, day);
        Calendar hoje = Calendar.getInstance();
        int idade = hoje.get(Calendar.YEAR) - dataNascimento.get(Calendar.YEAR);
        if (hoje.get(Calendar.DAY_OF_YEAR) < dataNascimento.get(Calendar.DAY_OF_YEAR)) {
            idade--;
        }
        return idade >= 18;
    }

    private void configurarMascaras() {
        // Máscara Telefone
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

        // Máscara CPF
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

        // Máscara RG
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