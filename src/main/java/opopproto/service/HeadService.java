package opopproto.service;

import opopproto.model.Head;
import opopproto.repository.HeadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HeadService implements UserDetailsService {
    @Autowired
    private HeadRepository headRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public Head createHead(String login, String password, String name,
                           String surname, String patronymic, String documentsPath) {
        if (loadUserByUsername(login) != null) {
            throw new UsernameNotFoundException(String.format("Руководитель '%s' уже существует", login));
        }
        final Head head = Head.builder()
                .name(name)
                .surname(surname)
                .patronymic(patronymic)
                .password(passwordEncoder.encode(password))
                .login(login)
                .documentsPath(documentsPath)
                .build();

        return headRepository.save(head);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final Head head = headRepository.findByLogin(username);
        if(head == null){
            throw new UsernameNotFoundException(String.format("Пользователь '%s' не существует", username));
        }
        return head;
    }
}
