package com.flipperdevices.bridge.api.error

enum class FlipperBleServiceError {
    CONNECT_BLUETOOTH_DISABLED,
    CONNECT_DEVICE_NOT_STORED,
    CONNECT_BLUETOOTH_PERMISSION,
    CONNECT_TIMEOUT,
    CONNECT_REQUIRE_REBOUND,

    SERVICE_INFORMATION_NOT_FOUND,
    SERVICE_SERIAL_NOT_FOUND,

    SERVICE_INFORMATION_FAILED_INIT,
    SERVICE_SERIAL_FAILED_INIT,
}
