package com.mainstreetcode.teammate.adapters;

import androidx.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.AdViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.ContentAdViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InstallAdViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.TeamViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.UserViewHolder;
import com.mainstreetcode.teammate.model.Ad;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import java.util.List;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.CONTENT_AD;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.INSTALL_AD;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.USER;

/**
 * Adapter for {@link Team}
 */

public class UserAdapter extends InteractiveAdapter<InteractiveViewHolder, UserAdapter.AdapterListener> {

    private final List<Differentiable> items;

    public UserAdapter(List<Differentiable> items, AdapterListener listener) {
        super(listener);
        this.items = items;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public InteractiveViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return viewType == CONTENT_AD
                ? new ContentAdViewHolder(getItemView(R.layout.viewholder_grid_content_ad, viewGroup), adapterListener)
                : viewType == INSTALL_AD
                ? new InstallAdViewHolder(getItemView(R.layout.viewholder_grid_install_ad, viewGroup), adapterListener)
                : new UserViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), adapterListener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull InteractiveViewHolder viewHolder, int position) {
        Differentiable item = items.get(position);
        if (item instanceof Ad) ((AdViewHolder) viewHolder).bind((Ad) item);
        else if (item instanceof User) ((UserViewHolder) viewHolder).bind((User) item);
        else if (item instanceof Role) ((TeamViewHolder) viewHolder).bind(((Role) item).getTeam());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        Differentiable item = items.get(position);
        return item instanceof User ? USER : ((Ad) item).getType();
    }

    public interface AdapterListener extends InteractiveAdapter.AdapterListener {
        void onUserClicked(User item);
    }

}
