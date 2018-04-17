package com.azure.core

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.azure.core.network.ReachabilityManager

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals

@RunWith(AndroidJUnit4::class)
class ReachabilityManagerTest {

    companion object {
        const val timeout = 30.0
    }

    //region - Sample instrumented test

    @Test
    @Throws(Exception::class)
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.azure.core.test", appContext.packageName)
    }
    //endregion

    //region - Tests - Initialization
    @Test
    @Throws(Exception::class)
    fun testThatManagerCanBeInitializedFromHost() {
        // Given, When
        val manager = ReachabilityManager(host="localhost")

        // Then
        assertNotEquals(null, manager)
    }

    fun testThatManagerCanBeInitializedFromAddress() {
        // Given, When
        val manager = ReachabilityManager()

        // Then
        assertNotEquals(null, manager)
    }
    fun testThatHostManagerIsReachableOnWiFi() {
        // Given, When
        val manager = ReachabilityManager(host="localhost")

        // Then
//        assertEquals(ReachabilityManager.NetworkReachabilityStatus.reachableEthernetOrWifi, manager.networkReachabilityStatus)
//        assertEquals(true, manager.isReachable)
//        assertEquals(false, manager.isReachableOnWWAN)
//        assertEquals(true, manager.isReachableOnEthernetOrWiFi)
    }
        //endregion
/*
    func testThatHostManagerIsReachableOnWiFi() {
        // Given, When
        let manager = ReachabilityManager(host: "localhost")

        // Then
        XCTAssertEqual(manager?.networkReachabilityStatus, .reachable(.ethernetOrWiFi))
        XCTAssertEqual(manager?.isReachable, true)
        XCTAssertEqual(manager?.isReachableOnWWAN, false)
        XCTAssertEqual(manager?.isReachableOnEthernetOrWiFi, true)
    }
 */
}

/*

    func testThatHostManagerStartsWithReachableStatus() {
        // Given, When
        let manager = ReachabilityManager(host: "localhost")

        // Then
        XCTAssertEqual(manager?.networkReachabilityStatus, .reachable(.ethernetOrWiFi))
        XCTAssertEqual(manager?.isReachable, true)
        XCTAssertEqual(manager?.isReachableOnWWAN, false)
        XCTAssertEqual(manager?.isReachableOnEthernetOrWiFi, true)
    }

    func testThatAddressManagerStartsWithReachableStatus() {
        // Given, When
        let manager = ReachabilityManager()

        // Then
        XCTAssertEqual(manager?.networkReachabilityStatus, .reachable(.ethernetOrWiFi))
        XCTAssertEqual(manager?.isReachable, true)
        XCTAssertEqual(manager?.isReachableOnWWAN, false)
        XCTAssertEqual(manager?.isReachableOnEthernetOrWiFi, true)
    }

    func testThatHostManagerCanBeDeinitialized() {
        // Given
        var manager: ReachabilityManager? = ReachabilityManager(host: "localhost")

        // When
        manager = nil

        // Then
        XCTAssertNil(manager)
    }

    func testThatAddressManagerCanBeDeinitialized() {
        // Given
        var manager: ReachabilityManager? = ReachabilityManager()

        // When
        manager = nil

        // Then
        XCTAssertNil(manager)
    }

    // MARK: - Tests - Listener

    func testThatHostManagerIsNotifiedWhenStartListeningIsCalled() {
        // Given
        guard let manager = ReachabilityManager(host: "store.apple.com") else {
            XCTFail("manager should NOT be nil")
            return
        }

        let expectation = self.expectation(description: "listener closure should be executed")
        var networkReachabilityStatus: ReachabilityManager.NetworkReachabilityStatus?

        manager.listener = { status in
            guard networkReachabilityStatus == nil else { return }
            networkReachabilityStatus = status
            expectation.fulfill()
        }

        // When
        manager.startListening()
        waitForExpectations(timeout: timeout, handler: nil)

        // Then
        XCTAssertEqual(networkReachabilityStatus, .reachable(.ethernetOrWiFi))
    }

    func testThatAddressManagerIsNotifiedWhenStartListeningIsCalled() {
        // Given
        let manager = ReachabilityManager()
        let expectation = self.expectation(description: "listener closure should be executed")

        var networkReachabilityStatus: ReachabilityManager.NetworkReachabilityStatus?

        manager?.listener = { status in
            networkReachabilityStatus = status
            expectation.fulfill()
        }

        // When
        manager?.startListening()
        waitForExpectations(timeout: timeout, handler: nil)

        // Then
        XCTAssertEqual(networkReachabilityStatus, .reachable(.ethernetOrWiFi))
    }

    // MARK: - Tests - Network ReachabilityManager Status

    func testThatManagerReturnsNotReachableStatusWhenReachableFlagIsAbsent() {
        // Given
        let manager = ReachabilityManager()
        let flags: SCNetworkReachabilityFlags = [.connectionOnDemand]

        // When
        let networkReachabilityStatus = manager?.networkReachabilityStatusForFlags(flags)

        // Then
        XCTAssertEqual(networkReachabilityStatus, .notReachable)
    }

    func testThatManagerReturnsNotReachableStatusWhenConnectionIsRequired() {
        // Given
        let manager = ReachabilityManager()
        let flags: SCNetworkReachabilityFlags = [.reachable, .connectionRequired]

        // When
        let networkReachabilityStatus = manager?.networkReachabilityStatusForFlags(flags)

        // Then
        XCTAssertEqual(networkReachabilityStatus, .notReachable)
    }

    func testThatManagerReturnsNotReachableStatusWhenInterventionIsRequired() {
        // Given
        let manager = ReachabilityManager()
        let flags: SCNetworkReachabilityFlags = [.reachable, .connectionRequired, .interventionRequired]

        // When
        let networkReachabilityStatus = manager?.networkReachabilityStatusForFlags(flags)

        // Then
        XCTAssertEqual(networkReachabilityStatus, .notReachable)
    }

    func testThatManagerReturnsReachableOnWiFiStatusWhenConnectionIsNotRequired() {
        // Given
        let manager = ReachabilityManager()
        let flags: SCNetworkReachabilityFlags = [.reachable]

        // When
        let networkReachabilityStatus = manager?.networkReachabilityStatusForFlags(flags)

        // Then
        XCTAssertEqual(networkReachabilityStatus, .reachable(.ethernetOrWiFi))
    }

    func testThatManagerReturnsReachableOnWiFiStatusWhenConnectionIsOnDemand() {
        // Given
        let manager = ReachabilityManager()
        let flags: SCNetworkReachabilityFlags = [.reachable, .connectionRequired, .connectionOnDemand]

        // When
        let networkReachabilityStatus = manager?.networkReachabilityStatusForFlags(flags)

        // Then
        XCTAssertEqual(networkReachabilityStatus, .reachable(.ethernetOrWiFi))
    }

    func testThatManagerReturnsReachableOnWiFiStatusWhenConnectionIsOnTraffic() {
        // Given
        let manager = ReachabilityManager()
        let flags: SCNetworkReachabilityFlags = [.reachable, .connectionRequired, .connectionOnTraffic]

        // When
        let networkReachabilityStatus = manager?.networkReachabilityStatusForFlags(flags)

        // Then
        XCTAssertEqual(networkReachabilityStatus, .reachable(.ethernetOrWiFi))
    }

    #if os(iOS)
    func testThatManagerReturnsReachableOnWWANStatusWhenIsWWAN() {
        // Given
        let manager = ReachabilityManager()
        let flags: SCNetworkReachabilityFlags = [.reachable, .isWWAN]

        // When
        let networkReachabilityStatus = manager?.networkReachabilityStatusForFlags(flags)

        // Then
        XCTAssertEqual(networkReachabilityStatus, .reachable(.wwan))
    }

    func testThatManagerReturnsNotReachableOnWWANStatusWhenIsWWANAndConnectionIsRequired() {
        // Given
        let manager = ReachabilityManager()
        let flags: SCNetworkReachabilityFlags = [.reachable, .isWWAN, .connectionRequired]

        // When
        let networkReachabilityStatus = manager?.networkReachabilityStatusForFlags(flags)

        // Then
        XCTAssertEqual(networkReachabilityStatus, .notReachable)
    }
    #endif
}

 */