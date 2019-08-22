//package br.com.smartfull.rest;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//
///**
// * Autor: Fred William Torno Junior
// * E-Mail: fredwilliam@gmail.com / fredwilliam@outlook.com
// * Site: www.fwtj.com.br
// * Telefone: (22) 9-8136-5786
// * Data: 02/04/2018
// * Hora: 13:24
// * Copyright©Fwtj Sistemas. Todos os direitos reservados.
// */
//@Component
//public class AuthProviderService implements AuthenticationProvider {
//
//    @Autowired
//    private UsuarioService usuarioService;
//
//
//    @Override
//    public Authentication authenticate(Authentication auth) throws AuthenticationException {
//        String login = auth.getName();
//        String senha = auth.getCredentials().toString();
//
//        if (login.equals("DEV") && senha.equals("DEV")){
//            UsuarioRest dev = new UsuarioRest(login, senha, "DEV");
//            return new UsernamePasswordAuthenticationToken(login, senha, getRoles(dev));
//        }
//
//        UsuarioRest busca = usuarioService.buscaUsuario(login, senha);
//
//        if (busca != null) {
//            return new UsernamePasswordAuthenticationToken(login, senha, getRoles(busca));
//        }else{
//            throw new UsernameNotFoundException("Login e/ou Senha inválidos.");
//        }
//
//        //throw new BadCredentialsException("Este usuário está desativado.");
//
//    }
//
//    @Override
//    public boolean supports(Class<?> auth) {
//        return auth.equals(UsernamePasswordAuthenticationToken.class);
//    }
//
//    private Collection<? extends GrantedAuthority> getRoles(UsuarioRest usuarioRest) {
//        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
//
//        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuarioRest.getRole()));
//
//        return authorities;
//    }
//
//}
