package opopproto.repository;

import opopproto.model.Head;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeadRepository extends JpaRepository<Head, Long> {
    Head findByLogin(String login);
}
