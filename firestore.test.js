const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read user cart", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("cart").get());
});

test("Authenticated user: Alice can create and read her own cart item", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const itemRef = aliceDb.collection("users").doc(ALICE_UID).collection("cart").doc("item_1");

  await assertSucceeds(
    itemRef.set({
      id: "item_1",
      userId: ALICE_UID,
      productId: "prod_101",
      name: "Wireless Earbuds",
      price: 2499,
      quantity: 1,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(itemRef.get());
});

test("Cross-user isolation: Bob cannot read or modify Alice's cart item", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("cart").doc("item_1").set({
      id: "item_1",
      userId: ALICE_UID,
      productId: "prod_101",
      name: "Wireless Earbuds",
      price: 2499,
      quantity: 1,
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const aliceItemRef = bobDb.collection("users").doc(ALICE_UID).collection("cart").doc("item_1");

  await assertFails(aliceItemRef.get());
  await assertFails(
    aliceItemRef.update({
      quantity: 5,
    })
  );
  await assertFails(aliceItemRef.delete());
});

test("Schema validation: Alice cannot write cart item with invalid or negative price", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const itemRef = aliceDb.collection("users").doc(ALICE_UID).collection("cart").doc("item_bad");

  await assertFails(
    itemRef.set({
      id: "item_bad",
      userId: ALICE_UID,
      productId: "prod_101",
      name: "Wireless Earbuds",
      price: -50,
      quantity: 1,
      createdAt: new Date(),
    })
  );
});

test("Orders: Alice can place an order and Bob cannot view it", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const orderRef = aliceDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_1");

  await assertSucceeds(
    orderRef.set({
      id: "order_1",
      userId: ALICE_UID,
      totalAmount: 2499,
      status: "CONFIRMED",
      deliveryAddress: "123 Market St, Bengaluru",
      itemsCount: 1,
      createdAt: new Date(),
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_1").get());
});
