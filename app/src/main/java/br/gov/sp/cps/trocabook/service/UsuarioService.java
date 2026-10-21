package br.gov.sp.cps.trocabook.service;

import com.google.firebase.Timestamp;

import java.util.Calendar;
import java.util.List;

import br.gov.sp.cps.trocabook.model.Usuario;
import br.gov.sp.cps.trocabook.repository.UsuarioRepository;

public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService() {
        this.usuarioRepository = new UsuarioRepository();
    }

    public interface ServiceCallback<T> {
        void onSucesso(T data);
        void onErro(String erro);
    }

    /**
     * Salva os dados do usuário no Firestore.
     *
     * O usuário do Firebase Authentication já deve ter sido criado
     * anteriormente. O ID recebido deve ser o UID desse usuário.
     */
    public void salvarDadosUsuario(
            String userId,
            String nome,
            String emailPrincipal,
            String emailRecuperacao,
            String telefone,
            String cpf,
            String rg,
            String nascimento,
            String genero,
            String fotoPerfil,
            ServiceCallback<Usuario> callback
    ) {

        if (userId == null || userId.trim().isEmpty()) {
            callback.onErro("Usuário inválido");
            return;
        }

        if (nome == null || nome.trim().isEmpty()) {
            callback.onErro("Nome inválido");
            return;
        }

        if (!validarEmailRecuperacao(emailPrincipal, emailRecuperacao)) {
            callback.onErro("Email de recuperação inválido");
            return;
        }

        if (!isCpfValido(cpf)) {
            callback.onErro("CPF inválido");
            return;
        }

        cpfExiste(cpf, new ServiceCallback<Boolean>() {

            @Override
            public void onSucesso(Boolean existe) {

                if (Boolean.TRUE.equals(existe)) {
                    callback.onErro("CPF já cadastrado");
                    return;
                }

                Usuario usuario = new Usuario();

                usuario.setId(userId);
                usuario.setNome(nome.trim());
                usuario.setCpf(cpf);
                usuario.setEmailPrincipal(emailPrincipal);
                usuario.setEmailRecuperacao(emailRecuperacao);
                usuario.setTelefone(telefone);
                usuario.setRg(rg);
                usuario.setNascimento(nascimento);
                usuario.setGenero(genero);
                usuario.setFotoPerfil(fotoPerfil);
                usuario.setDataCriacao(Timestamp.now());
                usuario.setRole(Usuario.Role.USER);
                usuario.setStatus(Usuario.StatusUsuario.ATIVO);


                // Usuário novo começa sem avaliação.
                usuario.setAvaliacao(0.0);

                usuarioRepository.cadastrar(
                        usuario,
                        new UsuarioRepository.Callback<Usuario>() {

                            @Override
                            public void onSucesso(Usuario usuarioSalvo) {
                                callback.onSucesso(usuarioSalvo);
                            }

                            @Override
                            public void onErro(Exception erro) {
                                callback.onErro(erro.getMessage());
                            }
                        }
                );
            }

            @Override
            public void onErro(String erro) {
                callback.onErro(erro);
            }
        });
    }

    public boolean validarEmailRecuperacao(
            String emailPrincipal,
            String emailRecuperacao
    ) {

        if (emailPrincipal == null || emailRecuperacao == null) {
            return false;
        }

        if (!emailRecuperacao.endsWith("@gmail.com")) {
            return false;
        }

        return !emailRecuperacao.equalsIgnoreCase(emailPrincipal);
    }

    public boolean isCpfValido(String cpf) {

        if (cpf == null) {
            return false;
        }

        cpf = cpf.replaceAll("\\D", "");

        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {

            int d1 = 0;
            int d2 = 0;
            int peso = 10;

            for (int i = 0; i < 9; i++) {

                int numero = Integer.parseInt(
                        cpf.substring(i, i + 1)
                );

                d1 += numero * peso;
                d2 += numero * (peso + 1);

                peso--;
            }

            int r1 = 11 - (d1 % 11);
            int digito1 = (r1 > 9) ? 0 : r1;

            d2 += digito1 * 2;

            int r2 = 11 - (d2 % 11);
            int digito2 = (r2 > 9) ? 0 : r2;

            return cpf.substring(9).equals(
                    "" + digito1 + digito2
            );

        } catch (Exception e) {
            return false;
        }
    }

    public boolean isMaiorDeIdade(
            int year,
            int month,
            int day
    ) {

        Calendar nascimento = Calendar.getInstance();
        nascimento.set(year, month, day);

        Calendar hoje = Calendar.getInstance();

        int idade =
                hoje.get(Calendar.YEAR)
                        - nascimento.get(Calendar.YEAR);

        if (hoje.get(Calendar.DAY_OF_YEAR)
                < nascimento.get(Calendar.DAY_OF_YEAR)) {

            idade--;
        }

        return idade >= 18;
    }

    public void usuarioExiste(
            String userId,
            ServiceCallback<Boolean> callback
    ) {

        if (userId == null || userId.trim().isEmpty()) {
            callback.onErro("Usuário inválido");
            return;
        }

        usuarioRepository.buscarPorUid(
                userId,
                new UsuarioRepository.Callback<Usuario>() {

                    @Override
                    public void onSucesso(Usuario usuario) {
                        callback.onSucesso(usuario != null);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onErro(erro.getMessage());
                    }
                }
        );
    }

    public void cpfExiste(
            String cpf,
            ServiceCallback<Boolean> callback
    ) {

        if (cpf == null || cpf.trim().isEmpty()) {
            callback.onErro("CPF inválido");
            return;
        }

        usuarioRepository.buscarPorCpf(
                cpf,
                new UsuarioRepository.Callback<Usuario>() {

                    @Override
                    public void onSucesso(Usuario usuario) {
                        callback.onSucesso(usuario != null);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onErro(erro.getMessage());
                    }
                }
        );
    }

    public void buscarUsuarioPorId(
            String userId,
            ServiceCallback<Usuario> callback
    ) {

        if (userId == null || userId.trim().isEmpty()) {
            callback.onErro("Usuário inválido");
            return;
        }

        usuarioRepository.buscarPorUid(
                userId,
                new UsuarioRepository.Callback<Usuario>() {

                    @Override
                    public void onSucesso(Usuario usuario) {
                        callback.onSucesso(usuario);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onErro(erro.getMessage());
                    }
                }
        );
    }

    public void buscarUsuarioPorEmail(
            String email,
            ServiceCallback<Usuario> callback
    ) {

        if (email == null || email.trim().isEmpty()) {
            callback.onErro("Email inválido");
            return;
        }

        usuarioRepository.buscarPorEmail(
                email.trim(),
                new UsuarioRepository.Callback<Usuario>() {

                    @Override
                    public void onSucesso(Usuario usuario) {
                        callback.onSucesso(usuario);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onErro(erro.getMessage());
                    }
                }
        );
    }

    public void buscarUsuarios(
            ServiceCallback<List<Usuario>> callback
    ) {

        usuarioRepository.buscarTodos(
                new UsuarioRepository.Callback<List<Usuario>>() {

                    @Override
                    public void onSucesso(List<Usuario> usuarios) {
                        callback.onSucesso(usuarios);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onErro(erro.getMessage());
                    }
                }
        );
    }

    public void buscarMelhoresAvaliados(
            ServiceCallback<List<Usuario>> callback
    ) {

        usuarioRepository.buscaTop6Avaliacao(
                new UsuarioRepository.Callback<List<Usuario>>() {

                    @Override
                    public void onSucesso(List<Usuario> usuarios) {
                        callback.onSucesso(usuarios);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onErro(erro.getMessage());
                    }
                }
        );
    }

    public void buscarUsuariosPorIds(
            List<String> ids,
            ServiceCallback<List<Usuario>> callback
    ) {

        if (ids == null || ids.isEmpty()) {
            callback.onSucesso(new java.util.ArrayList<>());
            return;
        }

        usuarioRepository.buscarPorUids(
                ids,
                new UsuarioRepository.Callback<List<Usuario>>() {

                    @Override
                    public void onSucesso(List<Usuario> usuarios) {
                        callback.onSucesso(usuarios);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onErro(erro.getMessage());
                    }
                }
        );
    }

    public void atualizarUsuario(
            Usuario usuario,
            ServiceCallback<Usuario> callback
    ) {

        if (usuario == null
                || usuario.getId() == null
                || usuario.getId().trim().isEmpty()) {

            callback.onErro("Usuário inválido");
            return;
        }

        usuarioRepository.atualizar(
                usuario,
                new UsuarioRepository.Callback<Usuario>() {

                    @Override
                    public void onSucesso(Usuario usuarioAtualizado) {
                        callback.onSucesso(usuarioAtualizado);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onErro(erro.getMessage());
                    }
                }
        );
    }

    public void deletarUsuario(
            String userId,
            ServiceCallback<Void> callback
    ) {

        if (userId == null || userId.trim().isEmpty()) {
            callback.onErro("Usuário inválido");
            return;
        }

        usuarioRepository.deletar(
                userId,
                new UsuarioRepository.Callback<Void>() {

                    @Override
                    public void onSucesso(Void resultado) {
                        callback.onSucesso(null);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onErro(erro.getMessage());
                    }
                }
        );
    }
}