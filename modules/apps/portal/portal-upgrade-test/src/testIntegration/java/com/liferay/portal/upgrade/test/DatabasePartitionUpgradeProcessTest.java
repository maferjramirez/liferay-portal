/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.upgrade.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.PropsTestUtil;
import com.liferay.portal.kernel.upgrade.DummyUpgradeProcess;
import com.liferay.portal.kernel.upgrade.UpgradeException;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Props;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import java.util.concurrent.FutureTask;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Luis Ortiz
 */
@RunWith(Arquillian.class)
public class DatabasePartitionUpgradeProcessTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() {
		_originalProps = PropsUtil.getProps();
	}

	@AfterClass
	public static void tearDownClass() {
		PropsUtil.setProps(_originalProps);
	}

	@Test
	public void testUpgradeWithDatabasePartitionDisabled()
		throws UpgradeException {

		PropsTestUtil.setProps(
			HashMapBuilder.<String, Object>put(
				"database.partition.enabled", "false"
			).put(
				"database.partition.thread.pool.enabled", "true"
			).build());

		UpgradeProcess upgradeProcess = new AssertConnectionUpgradeProcess();

		upgradeProcess.upgrade();
	}

	@Test
	public void testUpgradeWithDatabasePartitionEnabled()
		throws UpgradeException {

		PropsTestUtil.setProps(
			HashMapBuilder.<String, Object>put(
				"database.partition.enabled", "true"
			).put(
				"database.partition.thread.pool.enabled", "true"
			).build());

		UpgradeProcess upgradeProcess = new AssertConnectionUpgradeProcess();

		upgradeProcess.upgrade();
	}

	private static Props _originalProps;

	private class AssertConnectionUpgradeProcess extends DummyUpgradeProcess {

		@Override
		protected void process(UnsafeConsumer<Long, Exception> unsafeConsumer)
			throws Exception {

			if (GetterUtil.getBoolean(
					PropsUtil.get("database.partition.enabled"))) {

				Assert.assertNotSame(_getConnection(), _getConnection());
			}
			else {
				Assert.assertSame(_getConnection(), _getConnection());
			}
		}

		private Connection _getConnection() throws Exception {
			FutureTask<Connection> futureTask = new FutureTask<>(
				() -> {
					DatabaseMetaData databaseMetaData =
						connection.getMetaData();

					return databaseMetaData.getConnection();
				});

			Thread thread = new Thread(futureTask);

			thread.start();

			return futureTask.get();
		}

	}

}