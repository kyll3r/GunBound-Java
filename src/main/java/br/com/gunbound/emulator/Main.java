package br.com.gunbound.emulator;

import java.util.Arrays;

import br.com.gunbound.emulator.utils.Utils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;

public class Main {

    public static void main(String[] args) throws Exception {
        byte[] raw = Utils.hexStringToByteArray(
                "CE2F516F95EC310069141F2C5705F2D31B0015F30145011480359CA4A1A7FC12EBD0824DFC60F0F37A253A1B00C41E0145011480367B5AA3E1594BE2323CAC73E6E7AC00B9581B00734A0145011480374E4437D445319E1CCF157AE7172606E85F"
            );

            // Calcula o próximo múltiplo de 16
            int paddingLength = 16 - (raw.length % 16);
            if (paddingLength != 16) { // Se já for múltiplo de 16, não precisa preencher
                byte[] padded = new byte[raw.length + paddingLength];
                System.arraycopy(raw, 0, padded, 0, raw.length);
                raw = padded; // agora raw está com padding
            }

            byte[] decryptedPayload = GunBoundCipher.gunboundDynamicDecrypt(
                raw,
                "test", "1234",
                Utils.hexStringToByteArray("7B43A411"),
                0x4410
            );

            System.out.println("Descriptografado: " + Utils.bytesToHex(decryptedPayload));
    }
}
