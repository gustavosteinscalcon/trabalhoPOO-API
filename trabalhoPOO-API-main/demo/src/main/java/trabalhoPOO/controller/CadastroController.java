package trabalhoPOO.controller;

import trabalhoPOO.model.Usuario;
import trabalhoPOO.repository.UsuarioRepository;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CadastroController {

    private final UsuarioRepository usuarioRepository;

    public CadastroController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Abre a página de cadastro
    @GetMapping("/cadastro")
    public String mostrarCadastro(Model model) {
        model.addAttribute("pagina", "cadastro");
        return "index";
    }

    // Recebe os dados do formulário
    @PostMapping("/cadastro")
    public String cadastrar(
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam String senha,
            Model model) {

        // Verifica se já existe usuário com esse e-mail
        if (usuarioRepository.findByEmail(email).isPresent()) {

            model.addAttribute(
                    "erro",
                    "Este e-mail já está cadastrado."
            );
                model.addAttribute("pagina", "cadastro");

                return "index";
        }

        // Cria o usuário
        Usuario usuario = new Usuario();

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(senha);

        // Salva no banco
        usuarioRepository.save(usuario);

        // Depois do cadastro, vai para o login
        return "redirect:/login";
    }
}
