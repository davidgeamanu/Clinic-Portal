import { useEffect } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { queryKeys } from "@/lib/query-keys";
import type { NotificationResponse } from "@/types/api";

/**
 * Subscribes to the notification service's Server-Sent-Events stream.
 *
 * When a notification arrives, the cached notification list is invalidated
 * (updating the bell badge and notification pages everywhere) and a toast is
 * shown. EventSource reconnects automatically if the connection drops.
 */
export function useNotificationStream() {
  const queryClient = useQueryClient();

  useEffect(() => {
    const source = new EventSource("/api/notifications/stream");

    const onNotification = (event: MessageEvent) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.notifications.mine() });
      try {
        const notification = JSON.parse(event.data) as NotificationResponse;
        toast.info(notification.message);
      } catch {
        // Malformed payload — the list invalidation above is still enough
      }
    };

    source.addEventListener("notification", onNotification);

    return () => {
      source.removeEventListener("notification", onNotification);
      source.close();
    };
  }, [queryClient]);
}
