import dev.samstevens.totp.code.DefaultCodeGenerator
import dev.samstevens.totp.code.DefaultCodeVerifier
import dev.samstevens.totp.code.HashingAlgorithm
import dev.samstevens.totp.secret.DefaultSecretGenerator
import dev.samstevens.totp.time.SystemTimeProvider
import org.junit.jupiter.api.Test


class Test {
    @Test
    fun testTOTP() {
        val secretGenerator = DefaultSecretGenerator(64)
        val secret = secretGenerator.generate()

        val timeProvider = SystemTimeProvider()

        val codeGenerator = DefaultCodeGenerator(HashingAlgorithm.SHA512)
        val verifier = DefaultCodeVerifier(codeGenerator, timeProvider)

        val code = codeGenerator.generate(secret, timeProvider.time.floorDiv(30))

        assert(verifier.isValidCode(secret, code)) { "Verification failed for secret: $secret and code: $code" }
    }
}