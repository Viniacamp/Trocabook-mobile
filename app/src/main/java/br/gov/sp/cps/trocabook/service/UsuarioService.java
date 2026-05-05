package br.gov.sp.cps.trocabook.service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import br.gov.sp.cps.trocabook.repository.UsuarioRepository;

public class UsuarioService {

    private UsuarioRepository repository;

    public interface ServiceCallback<T> {
        void onSuccess(T data);
        void onError(String erro);
    }

    public UsuarioService() {
        repository = new UsuarioRepository();
    }


    public void salvarDadosUsuario(
            String userId,
            String emailPrincipal,
            String emailRec,
            String telefone,
            String cpf,
            String rg,
            String nascimento,
            String genero,
            ServiceCallback<Void> callback
    ) {

        if (!validarEmailRecuperacao(emailPrincipal, emailRec)) {
            callback.onError("Email de recuperação inválido");
            return;
        }

        if (!isCpfValido(cpf)) {
            callback.onError("CPF inválido");
            return;
        }

        Map<String, Object> usuario = montarUsuario(
                emailPrincipal, emailRec, telefone, cpf, rg, nascimento, genero
        );

        repository.salvarUsuario(userId, usuario, new UsuarioRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void resultado) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(String erro) {
                callback.onError(erro);
            }
        });
    }


    private Map<String, Object> montarUsuario(
            String emailPrincipal,
            String emailRec,
            String telefone,
            String cpf,
            String rg,
            String nascimento,
            String genero
    ) {

        Map<String, Object> usuario = new HashMap<>();
        usuario.put("email_principal", emailPrincipal);
        usuario.put("email_recuperacao", emailRec);
        usuario.put("telefone", telefone);
        usuario.put("cpf", cpf);
        usuario.put("rg", rg);
        usuario.put("nascimento", nascimento);
        usuario.put("genero", genero);

        return usuario;
    }

    public boolean validarEmailRecuperacao(String principal, String rec) {
        if (rec == null || !rec.endsWith("@gmail.com")) return false;
        return !rec.equalsIgnoreCase(principal);
    }

    public boolean isCpfValido(String cpf) {
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

        } catch (Exception e) {
            return false;
        }
    }

    public boolean isMaiorDeIdade(int year, int month, int day) {

        Calendar nascimento = Calendar.getInstance();
        nascimento.set(year, month, day);

        Calendar hoje = Calendar.getInstance();

        int idade = hoje.get(Calendar.YEAR) - nascimento.get(Calendar.YEAR);

        if (hoje.get(Calendar.DAY_OF_YEAR) < nascimento.get(Calendar.DAY_OF_YEAR)) {
            idade--;
        }

        return idade >= 18;
    }

    public void usuarioExiste(String userId, ServiceCallback<Boolean> callback) {

        repository.usuarioExiste(userId, new UsuarioRepository.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean existe) {
                callback.onSuccess(existe);
            }

            @Override
            public void onError(String erro) {
                callback.onError(erro);
            }
        });
    }
}