import http from 'k6/http';
import {check, sleep} from 'k6';
import {Counter} from 'k6/metrics';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const mode = __ENV.MODE || 'smoke';
const unexpectedErrors = new Counter('unexpected_errors');

export const options = mode === 'load'
  ? {
      scenarios: {
        votes: {
          executor: 'constant-vus',
          vus: Number(__ENV.VUS || 50),
          duration: __ENV.DURATION || '30s',
        },
      },
      thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500', 'p(99)<1000'],
        unexpected_errors: ['count==0'],
      },
    }
  : {
      vus: 2,
      iterations: 10,
      thresholds: {
        http_req_failed: ['rate<0.01'],
        unexpected_errors: ['count==0'],
      },
    };

export function setup() {
  const created = http.post(
    `${baseUrl}/api/v1/agendas`,
    JSON.stringify({title: `k6 ${Date.now()}`, description: `mode=${mode}`}),
    {headers: {'Content-Type': 'application/json'}},
  );
  check(created, {'agenda created': (r) => r.status === 201});
  const agendaId = created.json('id');
  const session = http.post(
    `${baseUrl}/api/v1/agendas/${agendaId}/sessions`,
    JSON.stringify({durationMinutes: 30}),
    {headers: {'Content-Type': 'application/json'}},
  );
  check(session, {'session opened': (r) => r.status === 201});
  return {agendaId};
}

export default function (data) {
  const unique = `${__VU}`.padStart(3, '0') + `${__ITER}`.padStart(8, '0');
  const response = http.post(
    `${baseUrl}/api/v1/agendas/${data.agendaId}/votes`,
    JSON.stringify({associateId: unique.slice(-11), choice: __ITER % 2 ? 'SIM' : 'NAO'}),
    {headers: {'Content-Type': 'application/json'}},
  );
  if (!check(response, {'vote accepted': (r) => r.status === 201})) {
    unexpectedErrors.add(1);
  }
  sleep(0.05);
}

export function teardown(data) {
  const result = http.get(`${baseUrl}/api/v1/agendas/${data.agendaId}/results`);
  check(result, {
    'result available': (r) => r.status === 200,
    'votes persisted': (r) => Number(r.json('totalVotes')) > 0,
  });
}
