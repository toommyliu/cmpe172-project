#!/usr/bin/env bash

set -euo pipefail

# Runs the focused concurrency test for appointment slot booking.
# Pass condition: two simultaneous reservation attempts produce only one appointment.
./mvnw -Dtest=MySqlAppointmentRepositoryConcurrencyTest test
