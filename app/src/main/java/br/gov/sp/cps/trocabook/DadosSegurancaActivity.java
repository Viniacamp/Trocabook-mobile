package br.gov.sp.cps.trocabook;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dados_seguranca);

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
                Intent intent = new Intent(this, VerificacaoActivity.class);
                intent.putExtra("email", editEmailRec.getText().toString());
                startActivity(intent);
            }
        });
    }

    private void configurarValidacoesAoSair() {
        // Validação do Email ao sair
        editEmailRec.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) { // Se saiu do campo
                String email = editEmailRec.getText().toString().trim().toLowerCase();
                if (email.length() > 0 && (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || !email.endsWith("@gmail.com"))) {
                    layoutEmailRec.setError("Use um email @gmail.com");
                } else {
                    layoutEmailRec.setError(null);
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
        String email = editEmailRec.getText().toString().trim().toLowerCase();
        String cpf = editCPF.getText().toString().replaceAll("[^\\d]", "");

        if (!email.endsWith("@gmail.com")) {
            layoutEmailRec.setError("Use um email @gmail.com");
            erro = true;
        }
        if (!isCpfValido(cpf)) {
            layoutCPF.setError("CPF inválido");
            erro = true;
        }
        if (editNascimento.getText().toString().isEmpty()) {
            layoutNascimento.setError("Selecione a data");
            erro = true;
        }
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
            new DatePickerDialog(this, (view, year, month, day) -> {
                editNascimento.setText(String.format("%02d/%02d/%d", day, month + 1, year));
                layoutNascimento.setError(null);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
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
                String formatado = ""; int i = 0;
                for (char m : mask.toCharArray()) {
                    if (m != '#') { formatado += m; continue; }
                    try { formatado += str.charAt(i); } catch (Exception e) { break; }
                    i++;
                }
                editTelefone.setText(formatado);
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