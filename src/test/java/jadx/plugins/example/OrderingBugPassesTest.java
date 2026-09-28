package jadx.plugins.example;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end check against the locally-built jadx-core (see {@code jadxVersion = "dev"} in
 * build.gradle.kts, resolved from {@code mavenLocal()}).
 *
 * <p>
 * Registers {@value OrderingBugPasses#PASS_COUNT} interdependent decompile passes and runs a full
 * decompile through the real jadx pipeline. Against an <b>unfixed</b> jadx-core this throws
 * {@code IllegalArgumentException: "Comparison method violates its general contract!"} from
 * {@code PassMerge} / TimSort. Once the pass-ordering logic is fixed (topological sort), the
 * decompile completes and this test is GREEN.
 *
 * <p>
 * Rebuild+publish the local jadx before running:
 *
 * <pre>
 *   (cd ../jadx &amp;&amp; ./gradlew :jadx-commons:jadx-zip:publishToMavenLocal \
 *       :jadx-plugins:jadx-input-api:publishToMavenLocal \
 *       :jadx-plugins:jadx-dex-input:publishToMavenLocal \
 *       :jadx-plugins:jadx-smali-input:publishToMavenLocal \
 *       :jadx-core:publishToMavenLocal)
 * </pre>
 */
class OrderingBugPassesTest {

	@Test
	public void interdependentPassesDoNotBreakOrdering() throws Exception {
		JadxArgs args = new JadxArgs();
		args.getInputFiles().add(getSampleFile("hello.smali"));
		// enable the demo passes; keep the watermark comment pass out of the way
		args.getPluginOptions().put(JadxExamplePlugin.PLUGIN_ID + ".enable", "no");
		args.getPluginOptions().put(JadxExamplePlugin.PLUGIN_ID + ".orderingBugDemo", "yes");

		try (JadxDecompiler jadx = new JadxDecompiler(args)) {
			jadx.load();
			JavaClass cls = jadx.getClasses().get(0);
			String clsCode = cls.getCode();
			assertThat(clsCode).isNotBlank();
		}
	}

	private File getSampleFile(String fileName) throws URISyntaxException {
		URL file = getClass().getClassLoader().getResource("samples/" + fileName);
		assertThat(file).isNotNull();
		return new File(file.toURI());
	}
}
