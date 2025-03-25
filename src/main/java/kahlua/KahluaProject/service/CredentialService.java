package kahlua.KahluaProject.service;

import kahlua.KahluaProject.apipayload.code.status.ErrorStatus;
import kahlua.KahluaProject.domain.user.Credential;
import kahlua.KahluaProject.domain.user.User;
import kahlua.KahluaProject.dto.user.request.SignUpRequest;
import kahlua.KahluaProject.exception.GeneralException;
import kahlua.KahluaProject.repository.CredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CredentialService {

    private final PasswordEncoder passwordEncoder;
    private final CredentialRepository credentialRepository;

    public Credential createCredential(SignUpRequest signUpRequest) {
        if(!signUpRequest.getPassword().equals(signUpRequest.getPasswordCheck())) {
            throw new  GeneralException(ErrorStatus.PASSWORD_NOT_MATCH);
        }

        String hashedPassword = passwordEncoder.encode(signUpRequest.getPassword());
        Credential credential = Credential.builder()
                .hashedPassword(hashedPassword)
                .build();

        return credentialRepository.save(credential);
    }

    public void checkPassword(User user, String password) {
        Credential credential = user.getCredential();

        if(!passwordEncoder.matches(password, credential.getPassword())) {
            throw new GeneralException(ErrorStatus.PASSWORD_INVALID);
        }
    }
}
