package brackettree.reader;

import suite.processor.IntProcessor;
import suite.suite.Subject;

import java.util.Stack;

import static suite.suite.$uite.set$;

public class BracketTreeProcessor implements IntProcessor {

    enum State {
        BEFORE, TREE, FENCE, EXTEND_SIGN, FENCE_SIGN, CLOSE_SIGN
    }

    private int extendSign = '[';
    private int closeSign = ']';
    private int fenceSign = '"';
    private Stack<Subject> branch;
    private Subject $work;
    private State state;
    private StringBuilder primaryBuilder;
    private StringBuilder secondaryBuilder;
    private String fence;

    public BracketTreeProcessor() {
    }

    void appendSecondaryBuilder(String append, boolean skipEmpty) {
        if(skipEmpty && append.isEmpty()) return;
        if(secondaryBuilder == null) {
            secondaryBuilder = new StringBuilder(append);
        } else {
            secondaryBuilder.append(append);
        }
    }

    @Override
    public void getReady() {
        branch = new Stack<>();
        $work = set$();
        state = State.BEFORE;
        primaryBuilder = new StringBuilder();
        secondaryBuilder = new StringBuilder();
    }

    public void advance(int i) {
        switch (state) {
            case BEFORE:
                if(i == closeSign) {
                    state = State.EXTEND_SIGN;
                } else {
                    state = State.TREE;
                    advance(i);
                }
                break;
            case EXTEND_SIGN:
                extendSign = i;
                state = State.FENCE_SIGN;
                break;
            case FENCE_SIGN:
                fenceSign = i;
                state = State.CLOSE_SIGN;
                break;
            case CLOSE_SIGN:
                closeSign = i;
                state = State.TREE;
                break;
            case TREE:
                if (i == extendSign) {
                    Subject $newWork;
                    appendSecondaryBuilder(primaryBuilder.toString().trim() ,true);
                    if(secondaryBuilder != null) {
                        $newWork = $work.in(secondaryBuilder.toString()).set();
                    } else {
                        $work.inset($newWork = set$());
                    }
                    branch.add($work);
                    $work = $newWork;
                    primaryBuilder = new StringBuilder();
                    secondaryBuilder = null;
                 } else if (i == closeSign) {
                    appendSecondaryBuilder(primaryBuilder.toString().trim() ,true);
                    if(secondaryBuilder != null) {
                        $work.sate(secondaryBuilder.toString());
                    }
                    if(branch.empty()) state = State.BEFORE;
                    else $work = branch.pop();
                    primaryBuilder = new StringBuilder();
                    secondaryBuilder = null;
                } else if(i == fenceSign) {
                    fence = new String(new int[]{fenceSign}, 0, 1) + primaryBuilder.toString().trim();
                    primaryBuilder = new StringBuilder();
                    state = State.FENCE;
                } else {
                    primaryBuilder.appendCodePoint(i);
                }
                break;

            case FENCE:
                primaryBuilder.appendCodePoint(i);
                int fenceStartIndex = primaryBuilder.length() - fence.length();
                if (fenceStartIndex >= 0 && primaryBuilder.indexOf(fence, fenceStartIndex) != -1) {
                    appendSecondaryBuilder(primaryBuilder.substring(0, fenceStartIndex), false);
                    primaryBuilder = new StringBuilder();
                    state = State.TREE;
                }
                break;
        }
    }

    @Override
    public Subject finish() {
        if(state == State.TREE) {
            appendSecondaryBuilder(primaryBuilder.toString().trim() ,true);
            if(secondaryBuilder != null)
                $work.sate(secondaryBuilder.toString());
        } else if(state == State.FENCE) {
            appendSecondaryBuilder(primaryBuilder.toString(),false);
            $work.sate(secondaryBuilder.toString());
        }
        while (!branch.empty()) $work = branch.pop();
        return $work;
    }
}
