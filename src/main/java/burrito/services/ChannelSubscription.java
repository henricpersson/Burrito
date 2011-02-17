package burrito.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;

/**
 * This class describes a clients connection to the channel API. Each connected
 * client will have a corresponding {@link ChannelSubscription}. The subscription
 * is kept alive by resetting the timestamp in regular intervals. Subscriptions
 * with a timestamp older than 5 minutes are considered inactive and will not
 * receive any updates.
 *
 * Subscriptions older than two hours are automatically deleted by a cron job.
 *
 * @author henper
 */

public class ChannelSubscription extends Model {

	@Id(Generator.AUTO_INCREMENT)
	private Long id;

	private String clientId;

	private String channelId;

	private List<String> feedIds;

	private Date created = new Date();

	private Date timestamp = created;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public List<String> getFeedIds() {
		return feedIds;
	}

	public void setFeedIds(List<String> feedIds) {
		this.feedIds = feedIds;
	}

	public void addFeedId(String feedId) {
		if (feedIds == null) feedIds = new ArrayList<String>();
		this.feedIds.add(feedId);
	}

	public boolean hasFeedId(String feedId) {
		return feedIds != null && feedIds.contains(feedId);
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Gets all active subscriptions to a feed. A subscription is considered
	 * active if it has a timestamp that is less than 5 minutes old.
	 *
	 * @param siteId
	 * @param threadId
	 * @return
	 */
	public static List<ChannelSubscription> getSubscriptionsForFeed(String feedId) {
		// Gets all active subscriptions. A subscriptions is considered to be
		// active if it has a timestamp less than 5 minutes ago.
		// A channel is kept alive by a ping from the client every 2 minutes or so.
		// The most straightforward approach would be to treat a subscription as
		// timed out even after 2 minutes, but by testing with different devices
		// it's seems to be a good thing to be generous to ping failures

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -5);
		return all().filter("feedIds", feedId).filter("timestamp >", cal.getTime()).fetch();
	}

	/**
	 * Sets the timestamp to the current time, to keep the subscription alive.
	 * Every client will ping this method every minute or so.
	 */
	public void keepAlive() {
		this.timestamp = new Date();
	}

	private static Query<ChannelSubscription> all() {
		return Model.all(ChannelSubscription.class);
	}

	/**
	 * Gets a subscription by its channelId
	 * 
	 * @param channelId
	 * @return
	 */
	public static ChannelSubscription getByChannelId(String channelId) {
		return all().filter("channelId", channelId).get();
	}

	/**
	 * Gets all expired subscriptions. A subscription is considered to be
	 * expired after two hours. (See
	 * http://code.google.com/appengine/docs/java/channel/overview.html)
	 */
	public static List<ChannelSubscription> getAllExpired() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, -2);
		return all().filter("timestamp <", cal.getTime()).fetch();
	}

	@Override
	public String toString() {
		return "ChannelSubscription [channelId=" + channelId + ", clientId="
				+ clientId + ", created=" + created + ", feedIds=" + feedIds
				+ ", id=" + id + ", timestamp=" + timestamp + "]";
	}
}
