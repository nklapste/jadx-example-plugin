package jadx.plugins.example;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * End-to-end demonstration that the jadx pass-ordering (PassMerge / TimSort) crash exists in the
 * <b>published</b> jadx release this branch depends on (see {@code jadxVersion} in build.gradle.kts).
 *
 * <p>
 * Registers {@value OrderingBugPasses#PASS_COUNT} interdependent decompile passes and runs a full
 * decompile through the real jadx pipeline. {@code PassMerge} sorts passes with an invalid
 * {@code Comparator} (a partial dependency order, not a total order), so TimSort ({@code List.sort})
 * throws {@code IllegalArgumentException: "Comparison method violates its general contract!"}.
 *
 * <p>
 * This test is GREEN when the bug is present: it asserts that the crash is thrown. The companion
 * branch that depends on the locally-built (and fixed) jadx asserts the opposite - that the
 * decompile completes without throwing.
 */
class OrderingBugPassesTest {

	@Test
	public void publishedJadxCrashesOnInterdependentPasses() throws Exception {
		JadxArgs args = new JadxArgs();
		args.getInputFiles().add(getSampleFile("hello.smali"));
		// enable the demo passes; keep the watermark comment pass out of the way
		args.getPluginOptions().put(JadxExamplePlugin.PLUGIN_ID + ".enable", "no");
		args.getPluginOptions().put(JadxExamplePlugin.PLUGIN_ID + ".orderingBugDemo", "yes");

		Throwable thrown = catchThrowable(() -> {
			try (JadxDecompiler jadx = new JadxDecompiler(args)) {
				jadx.load();
			}
		});

		assertThat(thrown)
				.as("published jadx must exhibit the PassMerge/TimSort ordering crash")
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Comparison method violates its general contract");
	}

	private File getSampleFile(String fileName) throws URISyntaxException {
		URL file = getClass().getClassLoader().getResource("samples/" + fileName);
		assertThat(file).isNotNull();
		return new File(file.toURI());
	}
}
