package jadx.plugins.example;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;
import jadx.api.plugins.pass.JadxPassInfo;
import jadx.core.dex.visitors.IDexTreeVisitor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end check against the locally-built jadx-core (see {@code jadxVersion = "dev"} in
 * build.gradle.kts, resolved from {@code mavenLocal()}).
 *
 * <p>
 * Registers {@value OrderingBugPasses#PASS_COUNT} interdependent decompile passes and runs a full
 * decompile through the real jadx pipeline. Against an <b>unfixed</b> jadx-core this throws
 * {@code IllegalArgumentException: "Comparison method violates its general contract!"} from
 * {@code PassMerge} / TimSort. Once the pass-ordering logic is fixed, the decompile completes, every
 * declared runAfter/runBefore constraint holds in the merged pass list, and this test is GREEN.
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
			assertPassesInDeclaredOrder(jadx);

			JavaClass cls = jadx.getClasses().get(0);
			String clsCode = cls.getCode();
			assertThat(clsCode).isNotBlank();
		}
	}

	/**
	 * Check the merged decompile pass list (the order jadx runs passes in) against every
	 * runAfter/runBefore constraint declared by the demo passes, including built-in anchors.
	 */
	private static void assertPassesInDeclaredOrder(JadxDecompiler jadx) {
		List<String> order = jadx.getRoot().getPasses().stream()
				.map(IDexTreeVisitor::getName)
				.collect(Collectors.toList());
		for (DemoOrderingPass pass : OrderingBugPasses.build()) {
			JadxPassInfo info = pass.getInfo();
			String name = info.getName();
			assertThat(order).as("pass list: %s", order).containsOnlyOnce(name);
			int pos = order.indexOf(name);
			// built-in names can repeat (e.g. CodeShrinkVisitor); like jadx, resolve to the last one
			for (String dep : info.runAfter()) {
				assertThat(pos).as("%s must run after %s, pass list: %s", name, dep, order)
						.isGreaterThan(order.lastIndexOf(dep));
			}
			for (String dep : info.runBefore()) {
				assertThat(pos).as("%s must run before %s, pass list: %s", name, dep, order)
						.isLessThan(order.lastIndexOf(dep));
			}
		}
	}

	private File getSampleFile(String fileName) throws URISyntaxException {
		URL file = getClass().getClassLoader().getResource("samples/" + fileName);
		assertThat(file).isNotNull();
		return new File(file.toURI());
	}
}
