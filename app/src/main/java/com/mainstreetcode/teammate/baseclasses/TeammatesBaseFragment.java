package com.mainstreetcode.teammate.baseclasses;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.snackbar.Snackbar;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.ChoiceBar;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Message;
import com.mainstreetcode.teammate.model.UiState;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.Validator;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.functions.Consumer;
import com.tunjid.androidbootstrap.view.util.InsetFlags;

import androidx.annotation.DrawableRes;
import androidx.annotation.MenuRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentTransaction;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Base Fragment for this app
 */

public class TeammatesBaseFragment extends BaseFragment implements View.OnClickListener {

    protected static final InsetFlags VERTICAL = InsetFlags.create(true, false, true, true);
    protected static final InsetFlags NONE = InsetFlags.NONE;
    protected static final int PLACE_PICKER_REQUEST = 1;

    protected static final Validator VALIDATOR = new Validator();

    protected CompositeDisposable disposables = new CompositeDisposable();
    protected io.reactivex.functions.Consumer<Throwable> emptyErrorHandler = ErrorHandler.EMPTY;
    protected ErrorHandler defaultErrorHandler;
    private Message lastMessage;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        defaultErrorHandler = ErrorHandler.builder()
                .defaultMessage(getString(R.string.error_default))
                .add(this::handleErrorMessage)
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) togglePersistentUi();
    }

    @Override
    public void onPause() {
        disposables.clear();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        disposables.clear();
        getPersistentUiController().setFabClickListener(null);
        super.onDestroyView();
    }

    public InsetFlags insetFlags() {
        return InsetFlags.VERTICAL;
    }

    public int[] staticViews() { return new int[]{}; }

    @StringRes
    protected int getFabStringResource() { return R.string.add_competitor; }

    @DrawableRes
    protected int getFabIconResource() { return R.drawable.ic_add_white_24dp; }

    @MenuRes
    protected int getToolbarMenu() { return 0; }

    @MenuRes
    protected int getAltToolbarMenu() { return 0; }

    public boolean showsFab() { return false; }

    public boolean showsToolBar() { return true; }

    public boolean showsAltToolBar() { return false; }

    public boolean showsBottomNav() { return true; }

    protected boolean showsSystemUI() { return true; }

    protected CharSequence getToolbarTitle() { return ""; }

    protected CharSequence getAltToolbarTitle() { return ""; }

    @Override
    public void onClick(View v) {}

    protected void toggleFab(boolean show) {getPersistentUiController().toggleFab(show);}

    protected void toggleProgress(boolean show) {getPersistentUiController().toggleProgress(show);}

    @SuppressWarnings("WeakerAccess")
    protected void setFabExtended(boolean extended) {getPersistentUiController().setFabExtended(extended);}

    protected void setToolbarTitle(CharSequence title) {getPersistentUiController().setToolbarTitle(title);}

    protected void showSnackbar(CharSequence message) {getPersistentUiController().showSnackBar(message);}

    protected void showSnackbar(Consumer<Snackbar> consumer) {getPersistentUiController().showSnackBar(consumer);}

    protected void showChoices(Consumer<ChoiceBar> consumer) {getPersistentUiController().showChoices(consumer);}

    @Nullable
    @Override
    @SuppressLint("CommitTransaction")
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        return beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out);
    }

    protected void setEnterExitTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition baseTransition = new Fade();

            setEnterTransition(baseTransition);
            setExitTransition(baseTransition);

            if (Config.isStaticVariant()) return;

            Transition baseSharedTransition = new TransitionSet()
                    .addTransition(new ChangeBounds())
                    .addTransition(new ChangeTransform())
                    .addTransition(new ChangeImageTransform())
                    .setOrdering(TransitionSet.ORDERING_TOGETHER);

            setSharedElementEnterTransition(baseSharedTransition);
            setSharedElementReturnTransition(baseSharedTransition);
        }
    }

    protected void removeEnterExitTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setEnterTransition(new Fade());
            setExitTransition(new Fade());
            setSharedElementEnterTransition(null);
            setSharedElementReturnTransition(null);
        }
    }

    protected void handleErrorMessage(Message message) {
        if (lastMessage == null || !lastMessage.equals(message)) showSnackbar(message.getMessage());
        lastMessage = message;
        toggleProgress(false);
    }

    @SuppressWarnings("unused")
    protected void updateFabOnScroll(int dx, int dy) {
        if (showsFab() && Math.abs(dy) > 3) toggleFab(dy < 0);
    }

    protected void onKeyBoardChanged(boolean appeared) {}

    public void togglePersistentUi() {
        getPersistentUiController().update(fromFragment(this));
    }

    @SuppressLint("CommitTransaction")
    @SuppressWarnings("ConstantConditions")
    protected final FragmentTransaction beginTransaction() {
        return getFragmentManager().beginTransaction();
    }

    @SuppressWarnings("WeakerAccess")
    protected PersistentUiController getPersistentUiController() {
        Activity activity = getActivity();
        return activity == null ? DUMMY : ((PersistentUiController) activity);
    }

    protected void hideKeyboard() {
        View root = getView();
        if (root == null) return;

        InputMethodManager imm = (InputMethodManager) root.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(root.getWindowToken(), 0);
    }

    private static UiState fromFragment(TeammatesBaseFragment fragment) {
        return new UiState(
                fragment.getFabIconResource(),
                fragment.getFabStringResource(),
                fragment.getToolbarMenu(),
                fragment.getAltToolbarMenu(),
                fragment.showsFab(),
                fragment.showsToolBar(),
                fragment.showsAltToolBar(),
                fragment.showsBottomNav(),
                fragment.showsSystemUI(),
                fragment.insetFlags(),
                fragment.getToolbarTitle(),
                fragment.getAltToolbarTitle(),
                fragment
        );
    }

    private static final PersistentUiController DUMMY = new PersistentUiController() {

        @Override
        public void update(UiState state) { }

        @Override
        public void toggleToolbar(boolean show) {}

        @Override
        public void toggleAltToolbar(boolean show) {}

        @Override
        public void toggleBottombar(boolean show) {}

        @Override
        public void toggleFab(boolean show) {}

        @Override
        public void toggleProgress(boolean show) {}

        @Override
        public void toggleSystemUI(boolean show) {}

        @Override
        public void setFabIcon(int icon, int textRes) {}

        @Override
        public void setFabExtended(boolean expanded) {}

        @Override
        public void showSnackBar(CharSequence message) {}

        @Override
        public void showSnackBar(Consumer<Snackbar> consumer) {}

        @Override
        public void showChoices(Consumer<ChoiceBar> consumer) {}

        @Override
        public void setToolbarTitle(CharSequence title) {}

        @Override
        public void setAltToolbarTitle(CharSequence title) {}

        @Override
        public void setAltToolbarMenu(int menu) {}

        @Override
        public void setFabClickListener(View.OnClickListener clickListener) {}
    };
}
