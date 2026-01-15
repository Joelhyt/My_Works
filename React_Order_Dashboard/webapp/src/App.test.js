import { expect, test, it } from 'vitest';

it('true to be truthy', () => {
  expect(true).toBe(true);
});

test('false to be falsy', () => {
  expect(false).toBe(false);
});
