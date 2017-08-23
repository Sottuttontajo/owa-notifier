/**
The MIT License (MIT)

Copyright (c) 2017 Mathieu GOULIN

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */package com.notification.manager;

import info.kapable.utils.owanotifier.utils.MathUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Timer;

import com.notification.Notification;
import com.notification.NotificationFactory.Location;

/**
 * A NotificationManager which slides old Notifications above or below new Notifications.
 */
public class QueueManager extends SimpleManager {
	private Timer m_timer;
	private int m_verticalPadding = 20;
	private double m_snapFactor = 0.2;
	private ScrollDirection m_scroll = ScrollDirection.SOUTH;

	/**
	 * Which way the new Notifications scroll.
	 */
	public enum ScrollDirection {
		NORTH, SOUTH
	}

	{
		m_timer = new Timer(50, new MovementManager());
		m_timer.start();
	}

	public QueueManager() {
		super();
	}

	public QueueManager(Location loc) {
		super(loc);
	}

	/**
	 * @return the padding in between Notifications in the queue
	 */
	public int getVerticalPadding() {
		return m_verticalPadding;
	}

	/**
	 * Sets the vertical padding between Notifications in the queue.
	 *
	 * @param verticalPadding
	 *            the padding that should be maintained between the bottom of the top Notification and the top of the
	 *            bottom Notification, in pixels
	 */
	public void setVerticalPadding(int verticalPadding) {
		m_verticalPadding = verticalPadding;
	}

	/**
	 * @return the proportion of remaining distance that a displaced notification should travel with each position
	 *         update, with a range of 0 (no movement) to 1 (immediately jumps to appropriate location)
	 */
	public double getSnapFactor() {
		return m_snapFactor;
	}

	/**
	 * Sets the speed with which old Notifications move out of the way when new Notifications are added.
	 *
	 * @param snapFactor
	 *            the proportion of remaining distance that a displaced notification should travel with each position
	 *            update, with a range of 0 (no movement) to 1 (immediately jumps to appropriate location)
	 */
	public void setSnapFactor(double snapFactor) {
		m_snapFactor = MathUtils.clamp(snapFactor, 0, 1);
	}

	/**
	 * @return the scroll direction
	 */
	public ScrollDirection getScrollDirection() {
		return m_scroll;
	}

	/**
	 * Sets the direction in which old Notifications will scroll after new ones have been added.
	 *
	 * @param dir
	 *            the direction to scroll in
	 */
	public void setScrollDirection(ScrollDirection dir) {
		m_scroll = dir;
	}

	/**
	 * Stops controlling the Notifications and leaves them in their current positions. They will still hide after the
	 * specified time, however.
	 */
	public void stop() {
		m_timer.stop();
	}

	private class MovementManager implements ActionListener {
		private int getPreviousShownIndex(List<Notification> notes, int startIndex) {
			for (int i = startIndex; i < notes.size(); i++) {
				if (notes.get(i).isShown())
					return i;
			}
			return -1;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			List<Notification> notes = getNotifications();
			if (notes.size() == 0)
				return;

			Location loc = getLocation();
			int startx = getScreen().getX(loc, notes.get(0));
			int starty = getScreen().getY(loc, notes.get(0));

			for (int i = notes.size() - 1; i >= 0; i--) {
				Notification note = notes.get(i);
				int prevIndex = getPreviousShownIndex(notes, i + 1);

				int dif = 0;
				int desdif = 0;
				if (prevIndex == -1) {
					dif = note.getY() - starty;
					desdif = 0;
				} else {
					Notification prev = notes.get(prevIndex);
					dif = note.getY() - prev.getY();
					desdif = prev.getHeight() + m_verticalPadding;
					if (m_scroll == ScrollDirection.NORTH) {
						desdif *= -1;
					}
				}

				int delta = desdif - dif;
				double moveAmount = delta * m_snapFactor;
				if (Math.abs(moveAmount) < 1) {
					moveAmount = MathUtils.sign(moveAmount);
				}
				note.setLocation(startx, note.getY() + (int) moveAmount);
			}
		}
	}
}
