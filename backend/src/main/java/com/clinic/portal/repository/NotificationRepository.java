package com.clinic.portal.repository;

import com.clinic.portal.model.Notification;
import com.clinic.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Notification bell: all notifications for a user, newest first
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    // Unread badge count: only unread notifications
    List<Notification> findByUserAndReadFalse(User user);

    // Unread count for the notification bell indicator
    long countByUserAndReadFalse(User user);
}
