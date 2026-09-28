package jadx.plugins.example;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a set of ~57 interdependent {@link DemoOrderingPass}es that reproduces the crash in
 * jadx's pass-ordering logic ({@code jadx.core.utils.PassMerge}).
 *
 * <p>
 * The crash is a {@code java.lang.IllegalArgumentException: "Comparison method violates its general
 * contract!"} thrown by TimSort ({@code List.sort}). {@code PassMerge} sorts custom passes with a
 * {@code Comparator} that encodes a partial dependency order rather than a total order, which
 * violates the {@code Comparator} general contract. It surfaces once there are enough passes with a
 * realistic mix of:
 * <ul>
 * <li>anchors to built-in visitors (so {@code ExtDepsComparator} yields a real -1/0/1 key), and</li>
 * <li>dense cross-references between custom passes (so {@code InvertedDepsComparator} contributes a
 * broken, non-antisymmetric {@code {0,1}} tie-break).</li>
 * </ul>
 */
public final class OrderingBugPasses {

	public static final int PASS_COUNT = 57;

	/** A few real built-in decompile-pass names the demo passes anchor to. */
	private static final String[] BUILTIN_ANCHORS = {
			"RegionMakerVisitor",
			"ModVisitor",
			"CodeShrinkVisitor",
			"EnumVisitor",
			"ClassModifier",
	};

	private OrderingBugPasses() {
	}

	public static List<DemoOrderingPass> build() {
		List<DemoOrderingPass> passes = new ArrayList<>(PASS_COUNT);
		for (int i = 0; i < PASS_COUNT; i++) {
			String name = passName(i);
			List<String> runAfter = new ArrayList<>();
			List<String> runBefore = new ArrayList<>();

			// Half the passes anchor to a real built-in visitor -> gives ExtDepsComparator a
			// genuine partition key.
			if (i % 2 == 0) {
				runAfter.add(BUILTIN_ANCHORS[i % BUILTIN_ANCHORS.length]);
			}

			// Dense, deterministic cross-references between the custom passes. Every edge points
			// from a lower- to a higher-indexed pass (runAfter earlier passes, runBefore later
			// passes), so the constraints are ACYCLIC and satisfiable - a valid ordering exists.
			// This mirrors "complicated before/after pairings" in a real multi-pass plugin tree
			// while still being dense enough to trigger the pre-fix TimSort crash.
			for (int j = 0; j < i; j++) {
				if ((i * 7 + j * 13) % 3 == 0) {
					runAfter.add(passName(j)); // this pass runs after an earlier one
				}
			}
			for (int k = i + 1; k < PASS_COUNT; k++) {
				if ((i * 5 + k * 11) % 4 == 0) {
					runBefore.add(passName(k)); // this pass runs before a later one
				}
			}
			passes.add(new DemoOrderingPass(name, runAfter, runBefore));
		}
		return passes;
	}

	public static String passName(int i) {
		return "DemoOrderingPass" + i;
	}
}
