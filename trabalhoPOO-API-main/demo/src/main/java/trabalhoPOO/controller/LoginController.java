package trabalhoPOO.controller;

import trabalhoPOO.model.Usuario;
import trabalhoPOO.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final UsuarioRepository usuarioRepository;

    public LoginController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        model.addAttribute("pagina", "login");
        return "index";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String senha,
            HttpSession session,
            Model model) {

        Usuario usuario = usuarioRepository
                .findByEmail(email)
                .orElse(null);

        if (usuario == null ||
            !usuario.getSenha().equals(senha)) {

            model.addAttribute(
                    "erro",
                    "E-mail ou senha incorretos."
            );
                model.addAttribute("pagina", "login");

                return "index";
        }

        // Guarda o usuário na sessão
        session.setAttribute("usuario", usuario);

        return "redirect:/jogo";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/login";
    }
}
