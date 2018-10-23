package examples;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class AnimatedGifExample {
	static String b64 = "R0lGODlhEAAQAPf/AMra5KO9zOzz9s3b5MDS3aS+zZOwwsrZ4+bt89jm7IKftuLr"
			+ "8ZCswH6bs9vn7qrD0+Ts8oqnvYKetomlu8PV4t7p79Pg6dzp79vm7q/F1aC6zdTi"
			+ "6bbL2XqYsdDf55SwxK/H1ajC0Yajudzp7q7E087c59Xi6niWr8XY4qW/0LnM2aO9"
			+ "zoyov7bK2djk7ICbtIOhtt7p7pq1yc3b5sDU38DR39vm7LDH1afA0Yqovebu85Gt"
			+ "wouovsvZ5oCdtunv9IaiuXyZscrb5qnC0Zezxdjj64WiuZ65y7vQ3N/p8OLq8avE"
			+ "1MTX4o6rwJaxxLnN29jm7a7F1dvm7Y2qwH2bs7zQ3Jizx9Df6djj7HmXr+Tt8rvO"
			+ "26/E1LrO3KC5zKW+z8/e577Q3oCctYShubTK2HyasZGtwHuYsdTg6enw89/p76zE"
			+ "04iku+Lr8LXJ2JKvw+Xt8tHf583b57vP3pu2ycjY45m0yNvp7qzE1bHI13qZsaK8"
			+ "zn2aspq0yODp8Nfi6svb5oaiuuDp76vE09Hf6ZWzxMfY4qnC087e5+rx9eLs8pSv"
			+ "xL/R3rPI2Nfj64ajuq7E1Y2pv7rQ2+Lp8J65yarC1JKvxLzR3nybsrzQ3unw9HmY"
			+ "sLvR3LPI17nO28rY5Iqlvc3c5urx9Iakus/f53iVrtPi6cnY436btNTi6tfi67PK"
			+ "1rHI2H6bspy3yJ63ydXi7JezxKjA0ZGuwNjk64CbtazE0Zezx8DR3NXi68TX4Nvn"
			+ "7e7097vP24CctH2atIqlvN7o7tPh6Nzn7JGvwK/F1MbX4dHg6MfY4b7S3c/e5n2b"
			+ "srjN2ezy9eDq78rb4svb473R2+Ts8cHV3r7R3eXu8rfN2JSvwo+tv9/p7pSxw6C7"
			+ "ytbj6cTX36zD1NHg5vL3+KnB0Onx9cPV35y4yLPJ1dnl7LnO2cHU3avD0bnP2sDV"
			+ "4pKvwdfk7O7z99Xi6XqXsNTi6N3o7sjY4ubv8s7c5qvE0env842svuLs8Z64yJm0"
			+ "xZq2x46svuLq8P///yH/C05FVFNDQVBFMi4wAwEAAAAh+QQFAAD/ACwAAAAAEAAQ"
			+ "AAAIvQD/CRR4poEEf0q0DFz478SZFwqAxPC3AAEChqU6GAQSwcHEihcFntAoQUQE"
			+ "BgkcVKCI4IdAkiYZfCiScmXFfzBzMFhYk6K/jScZ/uspSEFMJ0KxpIxxAQgPBk5k"
			+ "CDVRBIoDDE8/yNAg1MIGmglkyvDy5cHCEh4smIC14d/WLyGGQGpBA4UQtBYsCDyy"
			+ "AseDByBaqKhhqAfaKwNTPIiyBAQZFUjqGmY4KEMGEG5UdAlDgYlQgQ9IdOJQhRPD"
			+ "gAAh+QQFAAD/ACwAAAAAEAAQAAAI1gD/CRR4IkiZBjx0DFwoMIueBj58EBGlRAtD"
			+ "h1TEKAikgJUOfxAWbsKk0QgbFjmEJEkiEIFAVBLGjMoxZUcfG1AEhvz3YiOoSAz/"
			+ "3VHzD8EPIxNYMPgQVMqFSRAWsGjyhoidoAmkVFjgb8eHPq80BMWS4I4fQZboaFiB"
			+ "I6gJFxhGXDiyJ4WsIZUWItrwxwYGF/9ShHjwIEOLGkwAkbJgAgvgwHjwLLnB4QmF"
			+ "HnI8oNmwYSCJDCDycPBUA8UMMHGuMHxAQvSTTBTqzBASVCCIFlWQ1KDAMCAAIfkE"
			+ "BQAA/wAsAAAAABAAEAAACMsA/wkUeKZDmSANfAxc+K8BlRe+FNRSMIbKMoYwFCjI"
			+ "CAOGEY1iqgyMMIHkhJIRgE14JImXEoEMdpiJyYBBpBxN5mjyB+GfEytWiNyyspCR"
			+ "ohE8f2jQ4GUpw38JLvBE8CVFChxDnkadOmTQoRCQtEqFoKNYlAdrMohNyiVDlBst"
			+ "5ix0lAAphAX/uNwgoyIMDQpMdJWAgrSNQC6NODyZQ4NJnVAWCFeYNLDFli6MDM3w"
			+ "gCZXgl0XGFaRVAPFZjQmHEl5KhCaaQ+EKDAMCAAh+QQFAAD/ACwAAAAAEAAQAAAI"
			+ "2wD/CRRIJUiHLCdODFz47xEMCT5eNOBjMAjDKTkisBFh5J+CiK3KDFy06I2ZJix4"
			+ "TJggYowCPr8EHqEjg4iTDzuaTGExQYEVQP9WCN3j5cjCKXYuIfihZEiIEDhwMBSo"
			+ "KMYCLSSiHAqxZKoNB0kW/MvAZQmJDFP/jfi3AMEaEDcacZgqZSACEjfIcOjCZKEL"
			+ "DGr+IdDxz42KJ0hQCEFkQVUCB34WEP6nYssWGqfkxDH1B8MIfwv8DUQShsKnEoQ2"
			+ "FMEQI4lohhR6gLFg4q+DCmn/zQBzZUMqGwwDAgAh+QQFAAD/ACwAAAAAEAAQAAAI"
			+ "uAD/CRTIgAcQBQ06DFz474iMDwVFSEh4gmGKFRoeMogAZGKHUgMHPcDxxYsMJxsl"
			+ "UhSYIcqDEF8yosyh8sy/BxmWPBjyYGFKCS/OkABBFBLDfxsPNujkhkyLFkchcpTA"
			+ "QYVVGkeLJHAQw1+VLkhqoDi6tasSTmFoGBJSYuHWCv4WaPlHAUWPEh4smMDyNi4C"
			+ "gUzu5t2g1QHcBQj+CsSrtwgUw34VD7xiAVZZvz+OCtyQAMMFQf4YBgQAIfkEBQAA"
			+ "/wAsAAAAABAAEAAACNYA/wkUeMTSDhZGXgxc+C9Fij10PjSZoADVJoYk8IRIoaHP"
			+ "GxaBJGDKMvBBBjwPZK14RYQBqDFi9AwkAWLJgyE4NNj5EGmUAiokQeTJcyNDJYY5"
			+ "jMQ80YKD0xYM/01ho6BBkCpPPD2pEfVgIB9lkGSqQYFJ1D45FPhoQBZFD0BRbQhh"
			+ "RYQHhToz5JBCxBBKEh2idPybAcaDhQ0msCSQcudfEn9KBAqJg8bEHxeLL6gRCEHL"
			+ "wCsbsNjAcKfCpH8Qov7b4ALDCD8LICD4N1u1iwuC/C34wTAgACH5BAUAAP8ALAAA"
			+ "AAAQABAAAAjNAP8JFMil2JAvGpwMXPiPC5cMUQal0GCFQQSGLRrdiPLgUAovVnZM"
			+ "gDGwyhYOZG6sCYFDAxEzERQ0ECipyxMVLTJAGqLhFoMJCqj8o1CD0ZwwcxaGHPni"
			+ "DAoUhmjQYPjvpwJfHWZoZUKBKkwYCsp4GFunK8NIEWDUCkIIDZpQuqjmAGZEJgUT"
			+ "uSyUcLSQUROgY8b8c5QACpQEiBErmvMoqEApu0ZIvkD5wghNksQsG3ihgr/PoD/z"
			+ "qsJwUhsIqFNDUEJV4IIfOhAg+MEwIAAh+QQFAAD/ACwAAAAAEAAQAAAI1wD/CRRI"
			+ "Yk0GEkNWDFz4T4WbGyC4RAmx4sgihki2qCBzY8khinQWTRlIIcyWJxwakQiBY4+M"
			+ "NzkeCaRAgQaSLhwyLMHhhYiZCDD+zejx6RQKJguPOGnCRgIVMGBKyBHC8N8HFiJ8"
			+ "BLligVAcRFV38DDyosMGExtMWajaZMK/BllSuSjyR1XVKRMU8DlhA4PfBC4WTsGq"
			+ "9cQ/BzFGOMAg5Z+Nf3YmjCkrsEISf37U/FOs6JICBa2CDPS3YMHAJDEQWOFThqE/"
			+ "HQj+lV7wA9CvqgJhx9aihGFAACH5BAUAAP8ALAAAAAAQABAAAAjAAP8JFMipCodO"
			+ "JB4MXPiPCYUwXVS4AZEhwyCGJXqgoIFEBRkQS6I8SDHwioeMhmqoaAHiwQMcK44I"
			+ "tGDhpJCNLSANCfFFg4x/G2CZqFli4YMvXmR8YJAgQZENFhj+87mUBwYHUIqYkCrD"
			+ "CQMeQC7EcJAAi1SvEUQoEOSvAlmpDCIAkdDA34K2bwcyyPGPbod/CO66dbo0Ld2B"
			+ "PwLjTRBXhN8TAxEgEDhWrt9SDCXfjQFEwYszkKX+06LEH90zDAMCACH5BAkAAP8A"
			+ "LAAAAAAQABAAAAjWAP8JFEihBpIqLUAMXPhPyIw6FDI94ZCHxAOGV+KAmYGihieK"
			+ "IDKQGLhhAxoPcnpQmHhjCR48KQS6wGLCAilATGq0yPDgQYiYLjDY+LMB0cJKQ2Sl"
			+ "2HPkwggMLkww/IdjhQY6lgT5uZMAy1QNr/p82OFvQQUpCabaIfKmCYsFECZdkDL1"
			+ "AwMWE4z8QPBPzZ2pkUAFUvBCIASBUGz02TElx6gxElAJ5PsvSRIhOViwMaJADKZN"
			+ "CyH408FKwWAxVPRkYahFiSgiPnw0UD11oA4eDcoEOcEwIAAh+QQJAAD/ACwAAAAA"
			+ "EAAQAAAIyAD/CRw4zAYtYdwGKvyXz1m2YAaF1XsGbWE1aW3UQKQVb8CBA8YG3qMH"
			+ "Z4E8c++OKQMwD9k4XALTpClp7tsxD8oGIOs2DaamRDPbOFMIwBi6ZARwuQMKp83C"
			+ "fwSiVUEqgKnThdTU9YrWq5nVp73OMWNmzWvQp6tWlUv7j6m4AQpt2Utny1a4tmnc"
			+ "AQhAaVuAAAUABxaoCdwzfq70UaJEzpUrSo8HQjNQqNC+WLEKZda8cNYsIoWuFTLA"
			+ "bhaxpwK1EWOADV+/hQEBACH5BAUAAP8ALAAAAAAQABAAAAjcAP8JFGgj1YYrYGYM"
			+ "XPivggMMLkxYANODAsN//pLEwFBkA6ESnyiEQTLQ3wJ/IzD8MRVHzikaW7aoEKhj"
			+ "gR8HCVRZQCQEBZInKtz804HAnxqIC5l04UDmBgkEEPxdkHKRQ6MbINYgODkiwcUM"
			+ "JJZwybA1iQMbF5eEOBSFhJYFMRR5ZYgDR4gQQ5T8QHDJzpSFR7zsWUH4HyArCiaw"
			+ "mNJkxwcnRGTQOSLwFx8FY0RMmMCDRRMzbxYtGlim1QsfCmAYEcEmQo6/C4N0CMKn"
			+ "wWkJMB5dFHjiRJbZVBgGBAAh+QQFAAD/ACwAAAAAEAAQAAAIuQD/CRToT9AFDAk2"
			+ "DFz47weCBf5iOEgAy8IVhgge+qvgAEoRExY8lBiYESLHBEU2hCzRg4lAjSexgBTZ"
			+ "AwWFf1pMTlxYQoghGmE4KYm4kyGKGki6VCGKkuE/GiqicpAAJAKDD05btCDjplMD"
			+ "BVUZOIUEoiyJMy8kiLC68MGQB0syPPh3poHaHAycyNDwJcSDKBkEnuhgd21eGV6+"
			+ "4HgwaGApwlStfti7IgXDwYV5XJVxxKlAwmA1MwwIACH5BAUAAP8ALAAAAAAQABAA"
			+ "AAjVAP8JFPhjgT9BF1wMXPgPQUMIC/yMwOBiA0OBEP5NqnAHgw0sG64M1JLxn5oL"
			+ "UhK4+GMCTRwhApX4S/LvTkosJjZY8ABmxj8donQkgcIQESk5M+pQ4EGElRAbFwH1"
			+ "QEGhRgMfCnL0ucikaiYkZXwEYrHjYo0nnp5UCdJAAZspF1twmNvihB4xRnIwrJTh"
			+ "Rp48IP5loaJgVKQPdjTgGPJgCQgSA++OAcWAyKsVsh7gyfBgYBZMEsa+6aMhRQg8"
			+ "kBduQqVgQpMPdPakSHFR4AsjZC0dYRgQACH5BAUAAP8ALAAAAAAQABAAAAjRAP8J"
			+ "FFgBAQIdP2wMXPhPCYSHECG0GcaQBi9/GDNirNBuIA8xkjSNuEDywoiTuzYIpKLg"
			+ "0RxFCWLGhAIlgaN/D4womNCE0UJHJSzkMpHphQIjwFgw/KcrFBo0hCTUghHBy1JG"
			+ "dTx4CNVAAVUiS9cxmTGjRxlfO80spUHDEAoUE17AmLDDysIuYeYwqnHpH8sJDG5p"
			+ "CFGJSwsVT7pIEtg1ghkiGnCEWHODDIctWwbOreslxaEHUW40usEwAgMrGlIMipKB"
			+ "y5qlAp1o+DKExBeGAQEAIfkEBQAA/wAsAAAAABAAEAAACNcA/wkUqETLPwQIByr8"
			+ "xwPQjwUQD+rAsBATHysIYiQZCNHfwBWtFCi4pMjBiH9q/PhLkkBghxdjJtj5Z+Of"
			+ "FAwmYzj4xyaIDxEsmih0kQCDUSzw+CiYsGPhP1V/irhIlaXBvwmWnFowtcHElTIv"
			+ "jPD44FRIHEIWrvgESnahEDklwMygIoFNEydHFFJAcepTjzr/YEQwQ8QLjgfeOHRB"
			+ "QoMCBYFAcryRsQdHCBKNODzZEobDwCmL6KwIcWjJDTIqtiBZaObI6ChcQNxw48ap" + "wBVDSGRYs2ZhQAA7";
	
	private static final char[] map1 = new char[64];
	static {
		int i = 0;
		for (char c = 'A'; c <= 'Z'; c++)
			map1[i++] = c;
		for (char c = 'a'; c <= 'z'; c++)
			map1[i++] = c;
		for (char c = '0'; c <= '9'; c++)
			map1[i++] = c;
		map1[i++] = '+';
		map1[i++] = '/';
	}

	private static final byte[] map2 = new byte[128];
	static {
		for (int i = 0; i < map2.length; i++)
			map2[i] = -1;
		for (int i = 0; i < 64; i++)
			map2[map1[i]] = (byte) i;
	}

	public static byte[] decode(char[] in, int iOff, int iLen) {
		if (iLen % 4 != 0)
			throw new IllegalArgumentException("Length of Base64 encoded input string is not a multiple of 4.");
		while (iLen > 0 && in[iOff + iLen - 1] == '=')
			iLen--;
		int oLen = (iLen * 3) / 4;
		byte[] out = new byte[oLen];
		int ip = iOff;
		int iEnd = iOff + iLen;
		int op = 0;
		while (ip < iEnd) {
			int i0 = in[ip++];
			int i1 = in[ip++];
			int i2 = ip < iEnd ? in[ip++] : 'A';
			int i3 = ip < iEnd ? in[ip++] : 'A';
			if (i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127)
				throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
			int b0 = map2[i0];
			int b1 = map2[i1];
			int b2 = map2[i2];
			int b3 = map2[i3];
			if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0)
				throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
			int o0 = (b0 << 2) | (b1 >>> 4);
			int o1 = ((b1 & 0xf) << 4) | (b2 >>> 2);
			int o2 = ((b2 & 3) << 6) | b3;
			out[op++] = (byte) o0;
			if (op < oLen)
				out[op++] = (byte) o1;
			if (op < oLen)
				out[op++] = (byte) o2;
		}
		return out;
	}

	public static void main(String[] args) throws IOException {
		byte[] d = decode(b64.toCharArray(), 0, b64.length());
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setBounds(0, 0, 100, 100);

		AnimatedGif gif = new AnimatedGif(shell, SWT.NONE);
		gif.setLocation(10, 10);
		gif.setSize(16, 16);
		gif.load(new java.io.ByteArrayInputStream(d));
		shell.open();
		gif.animate();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
