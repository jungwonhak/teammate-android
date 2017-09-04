package com.mainstreetcode.teammates.notifications;


import android.app.Notification;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.TeamChat;
import com.mainstreetcode.teammates.model.TeamChatRoom;
import com.mainstreetcode.teammates.repository.ModelRespository;
import com.mainstreetcode.teammates.repository.TeamChatRepository;
import com.mainstreetcode.teammates.repository.TeamChatRoomRepository;
import com.mainstreetcode.teammates.repository.UserRepository;
import com.mainstreetcode.teammates.util.ErrorHandler;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.functions.Predicate;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;


public class TeamChatNotifier extends Notifier<TeamChat> {

    private static final int MAX_LINES = 5;
    private static TeamChatNotifier INSTANCE;

    private final UserRepository userRepository;
    private Team sender = Team.empty();

    private TeamChatNotifier() {
        userRepository = UserRepository.getInstance();
    }

    public static TeamChatNotifier getInstance() {
        if (INSTANCE == null) INSTANCE = new TeamChatNotifier();
        return INSTANCE;
    }

    @Override
    protected ModelRespository<TeamChat> getRepository() {
        return TeamChatRepository.getInstance();
    }

    @Override
    public Predicate<TeamChat> getNotificationFilter() {
        return teamChat -> !teamChat.getUser().equals(userRepository.getCurrentUser());
    }

    @Override
    protected void handleNotification(FeedItem<TeamChat> item) {
        TeamChatRoomRepository chatRoomRepository = TeamChatRoomRepository.getInstance();
        TeamChat chat = item.getModel();
        String roomId = chat.getTeamRoomId();

        chatRoomRepository.get(roomId).firstOrError()
                .flatMap(chatRoom -> fetchUnreadChats(item, chatRoom))
                .map(unreadChats -> buildNotification(item, unreadChats))
                .subscribe(this::sendNotification, ErrorHandler.EMPTY);
    }

    private Single<List<TeamChat>> fetchUnreadChats(FeedItem<TeamChat> item, TeamChatRoom chatRoom) {
        TeamChatRepository repository = TeamChatRepository.getInstance();
        sender.update(chatRoom.getTeam());
        return repository.fetchUnreadChats(item.getModel().getTeamRoomId())
                .subscribeOn(io())
                .observeOn(mainThread());
    }

    private Notification buildNotification(FeedItem<TeamChat> item, List<TeamChat> chats) {
        int size = chats.size();
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = getNotificationBuilder(item)
                .setContentIntent(getDeepLinkIntent(item))
                .setSound(defaultSoundUri)
                .setAutoCancel(true);

        if (size < 2) return notificationBuilder
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(item.getTitle())
                .setContentText(item.getBody())
                .build();

        int min = Math.min(size, MAX_LINES);
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

        for (int i = 0; i < min; i++) {
            TeamChat chat = chats.get(i);
            style.addLine(app.getString(R.string.chat_notification_multiline_item,
                    chat.getUser().getFirstName(), chat.getContent()));
        }

        if (size > MAX_LINES) {
            style.setSummaryText(app.getString(R.string.chat_notification_multiline_summary, (size - MAX_LINES)));
        }

        return notificationBuilder
                .setContentTitle(app.getString(R.string.chat_notification_multiline_title, size, sender.getName()))
                .setSmallIcon(R.drawable.ic_notification)
                .setStyle(style)
                .build();
    }
}
