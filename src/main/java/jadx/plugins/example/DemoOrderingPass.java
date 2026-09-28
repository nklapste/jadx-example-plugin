package jadx.plugins.example;

import java.util.List;

import jadx.api.plugins.pass.JadxPassInfo;
import jadx.api.plugins.pass.impl.OrderedJadxPassInfo;
import jadx.api.plugins.pass.types.JadxDecompilePass;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.nodes.RootNode;

/**
 * A no-op decompile pass whose only purpose is to carry a set of
 * {@code runAfter}/{@code runBefore} ordering constraints.
 *
 * <p>
 * {@link OrderingBugPasses} registers ~57 of these with intertwined constraints to reproduce the
 * TimSort "Comparison method violates its general contract!" crash in jadx's pass-ordering logic
 * ({@code jadx.core.utils.PassMerge}).
 */
public class DemoOrderingPass implements JadxDecompilePass {

	private final String name;
	private final List<String> runAfter;
	private final List<String> runBefore;

	public DemoOrderingPass(String name, List<String> runAfter, List<String> runBefore) {
		this.name = name;
		this.runAfter = runAfter;
		this.runBefore = runBefore;
	}

	@Override
	public JadxPassInfo getInfo() {
		return new OrderedJadxPassInfo(name, "demo ordering pass " + name, runAfter, runBefore);
	}

	@Override
	public void init(RootNode root) {
	}

	@Override
	public boolean visit(ClassNode cls) {
		return false;
	}

	@Override
	public void visit(MethodNode mth) {
	}
}
