import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

// Config via env
const TOTAL_RPS = Number(__ENV.TOTAL_RPS || 250000);
const INSTANCES = Number(__ENV.INSTANCES || 25);
const INSTANCE_ID = Number(__ENV.INSTANCE_ID || 0); // 1..N
const DURATION = __ENV.DURATION || '5m';

// compute local rate (integer division)
const LOCAL_RPS = Math.floor(TOTAL_RPS / INSTANCES);

// Basic sanity
if (INSTANCE_ID <= 0) {
  console.error('Por favor forneça INSTANCE_ID >= 1 como env var.');
  // proceed anyway but will produce identical userIds if not set
}

// Export dynamic options for k6
export const options = {
  // scenario: constant arrival rate on *this* agent
  scenarios: {
    load: {
      executor: 'constant-arrival-rate',
      rate: LOCAL_RPS,         // requests per timeUnit
      timeUnit: '1s',          // per second
      duration: DURATION,
      preAllocatedVUs: Math.max(1000, LOCAL_RPS * 2), // initial VUs
      maxVUs: Math.max(2000, LOCAL_RPS * 4),
    },
  },
  thresholds: {
    'http_req_duration{kind:vote-post}': ['p(95)<500', 'p(99)<1000'],
    'http_req_failed': ['rate<0.01'], // <1% errors
  },
};

// Target endpoints (configure)
const BASE = __ENV.BASE_URL || 'https://api.example.com';
const VOTE_PATH = __ENV.VOTE_PATH || '/v1/vote';

// Helper to construct user ids unique per generator
function makeUserId() {
  // instance-specific prefix to avoid collisions across generators
  const inst = INSTANCE_ID > 0 ? INSTANCE_ID : Math.floor(Math.random() * 100000);
  return `gen-${inst}-${uuidv4()}`;
}

// The default function will be the body of each "request"
export default function () {
  const userId = makeUserId();
  const voteId = uuidv4();
  const payload = JSON.stringify({
    vote_id: voteId,
    user_id: userId,
    event_id: __ENV.EVENT_ID || 'bb-season-2025',
    option_id: __ENV.OPTION_ID || 'contestant-42',
    client_ts: new Date().toISOString(),
  });

  const params = {
    headers: { 'Content-Type': 'application/json' },
    tags: { kind: 'vote-post' }, // used by thresholds
    timeout: '60s',
  };

  const res = http.post(`${BASE}${VOTE_PATH}`, payload, params);

  // basic checks and local logging
  check(res, {
    'vote accepted (2xx)': (r) => r.status >= 200 && r.status < 300,
  });

  // Optional: light sampling of leaderboard reads (very small fraction)
  // DON'T enable heavy reads in full-rate test unless you're also sizing read path.
  if (Math.random() < 0.001) { // 0.1% sampling; tune as needed
    const lb = http.get(`${BASE}/v1/event/${__ENV.EVENT_ID || 'bb-season-2025'}/leaderboard?option=${__ENV.OPTION_ID || 'contestant-42'}`, { timeout: '20s' });
    check(lb, { 'lb ok': (r) => r.status === 200 });
  }

  // k6 pacing: the constant-arrival-rate executor handles scheduling, so don't sleep long
  // small sleep to yield
  sleep(0.001);
}
